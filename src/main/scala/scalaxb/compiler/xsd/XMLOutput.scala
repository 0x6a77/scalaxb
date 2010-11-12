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

package scalaxb.compiler.xsd
import scala.collection.mutable

trait XMLOutput extends Args {  
  def buildXMLString(param: Param): String = {
    val ns = quoteNamespace(param.namespace)
    val name = "__obj." + makeParamName(param.name)
    
    // there shouldn't be nillable DataRecord, since it would be stored as DataRecord(ns, key, None).
    val nilElemCode = buildToXML("None.type", "None, " + ns + ", " + quote(Some(param.name)) + ", __scope, false") +
      "(NoneXMLWriter)"
    val xToXMLCode = buildToXML(param.baseTypeName, "x, " + 
      (param.typeSymbol match {
        case XsAny                => "x.namespace"
        case XsDataRecord(member) => "x.namespace"
        case _ => ns
      }) + ", " + 
      (param.typeSymbol match {
        case XsAny                => "x.key"
        case XsDataRecord(member) => "x.key"
        case _ => quote(Some(param.name))
      }) + ", __scope, " +
      (param.typeSymbol match {
        case XsAny => "true"
        case _     => "false"
      }))
    
    val retval = (param.cardinality, param.nillable) match {
      case (Multiple, true) =>
        name + " flatMap {" + newline +
          indent(5) + "case Some(x) => " + xToXMLCode + newline +
          indent(5) + "case None    => " + nilElemCode + newline +
          indent(4) + "}"
      case (Multiple, false) => name + " flatMap { x => " + xToXMLCode + " }"
      case (Optional, true)  => name + " map { _ map { x => " + xToXMLCode + " } getOrElse { " + 
          nilElemCode + " } } getOrElse { Nil }" 
      case (Optional, false) => name + " map { x => " + xToXMLCode + " } getOrElse { Nil }"
      case (Single, true)    => name + " map { x => " + xToXMLCode + " } getOrElse { " + nilElemCode + " }"
      case (Single, false) =>
        val elemLabel = param.typeSymbol match {
          case XsAny                => name + ".key"
          case XsDataRecord(member) => name + ".key"
          case _ => quote(Some(param.name))
        }
        buildToXML(param.baseTypeName, name + ", " + ns + ", " + elemLabel + ", __scope, " +
          (param.typeSymbol match {
            case XsAny => "true"
            case _     => "false"
          }))
      }
    
    retval
  }
  
  def buildAttributeString(attr: AttributeLike): String = attr match {
    case ref: AttributeRef => buildAttributeString(buildAttribute(ref))
    case x: AttributeDecl  => buildAttributeString(x)
    case any: AnyAttributeDecl => buildAttributeString(any)
    case group: AttributeGroupDecl => buildAttributeString(group)
  }
  
  def buildAttributeString(any: AnyAttributeDecl): String =
    "__obj." + makeParamName(ANY_ATTR_PARAM) + ".foreach { x =>" + newline +
    indent(4) + "attr = scala.xml.Attribute((x.namespace map { __scope.getPrefix(_) }).orNull, x.key.orNull, x.value, attr) }"
    
  def buildAttributeString(attr: AttributeDecl): String = {
    val namespaceString = if (attr.global) "__scope.getPrefix(" + quote(attr.namespace.orNull) + ")"
      else "null"
    val name = "__obj." + makeParamName(buildParam(attr).name)
        
    if (toMinOccurs(attr) == 0)
      name + " foreach { x => attr = scala.xml.Attribute(" + namespaceString + ", " + quote(attr.name) +
        ", " + buildToString("x", attr.typeSymbol) + ", attr) }"
    else attr.defaultValue match {
      case Some(x) =>
        "if (" + buildToString(name, attr.typeSymbol) + " != " + quote(x) + 
        ") attr = scala.xml.Attribute(" + namespaceString + ", " + quote(attr.name) + ", " + 
        buildToString(name, attr.typeSymbol) + ", attr)"
      case None =>
        "attr = scala.xml.Attribute(" + namespaceString + ", " + quote(attr.name) + ", " + 
        buildToString(name, attr.typeSymbol) + ", attr)"
    }
  }
  
  def buildAttributeString(group: AttributeGroupDecl): String =
    "attr = " + buildParam(group).baseTypeName + "Format" + ".toAttribute(__obj." + makeParamName(buildParam(group).name) +
    ", attr, __scope)"
    
  def buildToString(selector: String, typeSymbol: XsTypeSymbol): String = typeSymbol match {
    case symbol: BuiltInSimpleTypeSymbol if (buildTypeName(symbol) == "java.util.GregorianCalendar") ||
      (buildTypeName(symbol) == "Array[Byte]")  =>
      "scalaxb.Helper.toString(" + selector + ")"
    case symbol: BuiltInSimpleTypeSymbol => selector + ".toString"
    case ReferenceTypeSymbol(decl: SimpleTypeDecl) =>       
      decl.content match {
        case x: SimpTypListDecl => selector + ".map(x => " + buildToString("x", baseType(decl)) + ").mkString(\" \")" 
        case _ => buildToString(selector, baseType(decl))
      }
    case _ => selector + ".toString"
  }
}
