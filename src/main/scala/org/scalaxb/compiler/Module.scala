/*
 * Copyright (c) 2010 e.e d3si9n
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
 
package org.scalaxb.compiler

import org.github.scopt.OptionParser
import scala.collection.{Map, Set}
import scala.collection.mutable.{ListBuffer, ListMap}
import java.io.{File, BufferedReader, Reader, PrintWriter}

trait Logger {
  def log(msg: String) {
    println("["+msg+"]")
    Console.flush
  }
}

trait Module extends Logger {
  type Schema
  type Context
  
  val encoding = "UTF-8"
  private val config = new ModuleConfig()   
  
  class ModuleConfig {
    var verbose = false
    val packageNames: ListMap[Option[String], Option[String]] =
      ListMap.empty[Option[String], Option[String]]
    val wrappedComplexTypes: ListBuffer[String] = ListBuffer.empty[String]
    var outdir: File = new File(".")
    var classPrefix: Option[String] = None
    var paramPrefix: Option[String] = None
  }
  
  trait Importable {
    def targetNamespace: Option[String]
    def imports: Seq[String]
    def reader: Reader
    def toSchema(context: Context): Schema
  }
  
  def start(args: Seq[String]) { 
    val files = ListBuffer.empty[File]
    
    config.packageNames(None) = None
    val paramParser = new OptionParser("scalaxb") {
      opt("d", "outdir", "<directory>", "generated files will go into <directory>",
        { d: String => config.outdir = new File(d) })
      opt("p", "package", "<package>", "specifies the target package",
        { p: String => config.packageNames(None) = Some(p) })
      keyValueOpt("p", "package", "<namespaceURI>", "<package>",
        "specifies the target package for <namespaceURI>",
        { (key: String, value: String) => { config.packageNames(Some(key)) = Some(value) } })
      opt(None, "class-prefix", "<prefix>", "prefixes generated class names",
        { p: String => config.classPrefix = Some(p) })
      opt(None, "param-prefix", "<prefix>", "prefixes generated parameter names",
        { p: String => config.paramPrefix = Some(p) })
      opt(None, "wrap-contents", "<complexType>",
        "wraps inner contents into a seperate case class",
        { w: String => config.wrappedComplexTypes append w })
      opt("v", "verbose", "be extra verbose",
        { config.verbose = true })
      arglist("<schema_file>...", "input schema to be converted",
        { x: String => files append (new File(x)) })
    }
    
    if (paramParser.parse(args))
      processFiles(files.map(file =>
          (file, buildOutputFile(file, config.outdir))),
          config.packageNames,
          config.classPrefix, config.paramPrefix,
          config.wrappedComplexTypes.toList,
          config.verbose
        )
  }
  
  def process(file: File, output: File, packageName: Option[String],
      classPrefix: Option[String] = None, paramPrefix: Option[String] = None,
      verbose: Boolean = false) =
    processFiles(List((file, output)),
      Map[Option[String], Option[String]]((None, packageName)),
      classPrefix, paramPrefix, Nil, verbose)
      
  def processFiles(filePairs: Seq[(File, File)],
      packageNames: Map[Option[String], Option[String]],
      classPrefix: Option[String] = None, paramPrefix: Option[String] = None,
      wrappedComplexTypes: List[String] = Nil,
      verbose: Boolean = false) = {
    config.verbose = verbose
    
    val files = filePairs.map(_._1)
    files.foreach(file => if (!file.exists)
      error("file not found: " + file.toString))
      
    processReaders( (filePairs map { pair =>
        new BufferedReader(new java.io.InputStreamReader(
          new java.io.FileInputStream(pair._1), encoding)) ->
        new PrintWriter(new java.io.OutputStreamWriter(
          new java.io.FileOutputStream(pair._2), encoding)) 
      }).toMap,
      packageNames, classPrefix, paramPrefix, wrappedComplexTypes)
    val outfiles = ListBuffer.empty[File] ++ (filePairs map { pair =>
      pair._2
    })
    outfiles foreach { file =>
      println("generated " + file + ".")
    }
    
    if (filePairs.size > 0) {
      val parent = filePairs(0)._2.getParentFile
      val helper = new File(parent, "Helper.scala")
      copyFileFromResource("/Helper.scala", helper)
      outfiles += helper
    }
    
    outfiles.toList
  }
  
  def processReaders(inputToOutput: Map[Reader, PrintWriter],
      packageNames: Map[Option[String], Option[String]],
      classPrefix: Option[String], paramPrefix: Option[String],
      wrappedComplexTypes: List[String]) = {    
    val context = buildContext
    val importables = inputToOutput.keysIterator.toList map {
      toImportable(_)
    }
    
    val sorted = sortByDependency(importables)
    val schemas = ListMap.empty[Importable, Schema]
    val usedPackages = ListBuffer.empty[Option[String]]
    
    sorted foreach { importable =>
      schemas(importable) = parse(importable, context)
    }
    
    processContext(context, packageNames, classPrefix, paramPrefix, wrappedComplexTypes)
    
    sorted foreach { importable =>
      val schema = schemas(importable)
      val pkg = packageName(schema, context)
      
      val out = inputToOutput(importable.reader)
      try {
        generate(schema, context, out, pkg, !usedPackages.contains(pkg),
          classPrefix, paramPrefix, wrappedComplexTypes)
      }
      finally {
        out.flush()
        out.close()
      }
      
      usedPackages += pkg
    }
  }
  
  def toImportable(in: Reader): Importable
  
  def packageName(schema: Schema, context: Context): Option[String]
  
  def sortByDependency(files: Seq[Importable]): Seq[Importable] = {
    val schemaFiles = ListMap.empty[Option[String], Importable]

    files foreach { importable =>
      importable.targetNamespace foreach { targetNamespace =>
        schemaFiles += (importable.targetNamespace -> importable) 
      } 
    }

    val XML_URI = "http://www.w3.org/XML/1998/namespace"    
    val unsorted = ListBuffer.empty[Importable]
    unsorted.appendAll(files)
    val sorted = ListBuffer.empty[Importable]
    val upperlimit = unsorted.size * unsorted.size
    
    def containsAll(imports: Seq[String]) = imports.forall { namespace =>
      (namespace == XML_URI) || sorted.contains(schemaFiles(Some(namespace)))
    }

    for (i <- 0 to upperlimit) {
      if (unsorted.size > 0) {
        val importable = unsorted(i % unsorted.size)
        if ((importable.imports.size == 0) ||
            containsAll(importable.imports)) {
          unsorted -= importable
          sorted += importable
        } // if
      } // if
    }

    if (unsorted.size > 0)
      error("Circular import: " + unsorted.toList)
    sorted
  }

  def buildOutputFile(input: File, outdir: File) = {
    if (!input.exists)
      error("file not found: " + input.toString)
    
    val name = input.getName
    val namepart = name.splitAt(name.indexOf('.'))._1
    new File(outdir, namepart + ".scala") 
  }

  def buildContext: Context
  
  def processContext(context: Context,
      packageNames: collection.Map[Option[String], Option[String]],
      classPrefix: Option[String], paramPrefix: Option[String],
      wrapped: List[String]): Unit
  
  def parse(importable: Importable, context: Context): Schema
    = importable.toSchema(context)
    
  def parse(in: Reader): Schema
    = parse(toImportable(in), buildContext)
  
  def parse(file: File): Schema
    = parse(new BufferedReader(
      new java.io.InputStreamReader(
        new java.io.FileInputStream(file), encoding)))
  
  def generate(schema: Schema, context: Context, output: PrintWriter,
    packageName: Option[String], firstOfPackage: Boolean,
    classPrefix: Option[String], paramPrefix: Option[String],
    wrappedComplexTypes: List[String]): PrintWriter
  
  override def log(msg: String) {
    if (config.verbose) {
      println("["+msg+"]")
      Console.flush
    }
  }
  
  def copyFileFromResource(source: String, dest: File) {
    val in = getClass.getResourceAsStream(source)
    val reader = new java.io.BufferedReader(new java.io.InputStreamReader(in))
    val out = new java.io.PrintWriter(new java.io.FileWriter(dest))
    var line: Option[String] = None
    line = Option[String](reader.readLine)
    while (line != None) {
      line foreach { out.println }
      line = Option[String](reader.readLine)
    }
    in.close
    out.flush
  }
}
