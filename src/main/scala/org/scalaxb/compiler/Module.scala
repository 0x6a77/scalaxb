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
import collection.mutable.{ListBuffer, ListMap}
import java.io.{File}

trait Logger {
  def log(msg: String) {
    println("["+msg+"]")
    Console.flush
  }
}

trait Module extends Logger {
  type Schema
  type Context
  
  private val config = new ModuleConfig()   
  
  class ModuleConfig {
    var verbose = false
    var packageName: Option[String] = None
    var outdir: File = new File(".")  
  }
  
  def start(args: Seq[String]) { 
    val files = ListBuffer.empty[File]
    val paramParser = new OptionParser("scalaxb") {
      opt("d", "outdir", "<directory>", "generated files will go into <directory>",
        { d: String => config.outdir = new File(d) })
      opt("p", "package", "<package>", "specifies the target package",
        { p: String => config.packageName = Some(p) })
      opt("v", "verbose", "be extra verbose",
        { config.verbose = true })
      arglist("<schema_file>...", "input schema to be converted",
        { x: String => files += new File(x) })
    }
    
    if (paramParser.parse(args))
      processFiles(files.map(file =>
          (file, buildOutputFile(file, config.outdir))),
        Map[String, Option[String]]((null, config.packageName))
        )
  }
  
  def processFiles(filePairs: Seq[(File, File)],
      packageNames: Map[String, Option[String]]) = {
    val files = filePairs.map(_._1)
    files.foreach(file => if (!file.exists)
      error("file not found: " + file.toString))
    
    val context = buildContext
    val sorted = sortByDependency(files)
    val schemas = ListMap.empty[File, Schema]
    val outfiles = ListBuffer.empty[File]
    val outputs = ListMap.empty[File, File] ++= filePairs.map(x => x._1 -> x._2)
    val usedPackages = ListBuffer.empty[Option[String]]
    
    for (file <- sorted) 
      schemas += (file -> parse(file, context))
    
    processContext(context, packageNames)
    
    for (file <- sorted) {
      val schema = schemas(file)
      val pkg = packageName(schema, context)
      outfiles += generate(schema, context, outputs(file),
        pkg, !usedPackages.contains(pkg))
      usedPackages += pkg
    }
    outfiles.toList
  }
  
  def packageName(schema: Schema, context: Context): Option[String]
  
  def process(file: File, output: File, packageName: Option[String]) =
    processFiles(List((file, output)),
      Map[String, Option[String]]((null, packageName)))(0)
  
  def sortByDependency(files: Seq[File]): Seq[File] =
    files

  def buildOutputFile(input: File, outdir: File) = {
    val name = input.getName
    val namepart = name.splitAt(name.indexOf('.'))._1
    new File(outdir, namepart + ".scala") 
  }

  def buildContext: Context
  
  def processContext(context: Context,
      packageNames: Map[String, Option[String]]): Unit
  
  def parse(input: File, context: Context): Schema
  
  def parse(input: File): Schema
    = parse(input, buildContext)
  
  def generate(schema: Schema, context: Context, output: File,
    packageName: Option[String], firstOfPackage: Boolean): File
  
  override def log(msg: String) {
    if (config.verbose) {
      println("["+msg+"]")
      Console.flush
    }
  }
}
