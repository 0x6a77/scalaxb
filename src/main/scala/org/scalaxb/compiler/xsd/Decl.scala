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

package org.scalaxb.compiler.xsd

import scala.collection.{Map, Set}
import scala.collection.mutable
import scala.collection.immutable

abstract class Decl

class ParserConfig {
  var scope: scala.xml.NamespaceBinding = _
  var targetNamespace: String = null
  val topElems  = mutable.ListMap.empty[String, ElemDecl]
  val elemList  = mutable.ListBuffer.empty[ElemDecl]
  val types     = mutable.ListMap.empty[String, TypeDecl]
  val attrs     = mutable.ListMap.empty[String, AttributeDecl]
  val choices   = mutable.Set.empty[ChoiceDecl]

  def containsType(name: String) = {
    val (namespace, typeName) = TypeSymbolParser.splitTypeName(name, this)
    if (namespace == targetNamespace)
      types.contains(typeName)
    else {
      println(namespace + ":" + typeName + " was not found")
      false
    }
  }

  def getType(name: String): TypeDecl = {
    val (namespace, typeName) = TypeSymbolParser.splitTypeName(name, this)
    if (namespace == targetNamespace)
      types(typeName)
    else
      null
  }
}

object TypeSymbolParser {
  val XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema"
  val XML_URI = "http://www.w3.org/XML/1998/namespace"
  
  def fromString(name: String, config: ParserConfig): XsTypeSymbol = {
    val (namespace, typeName) = splitTypeName(name, config)
    namespace match {
      case XML_SCHEMA_URI => XsTypeSymbol.toTypeSymbol(typeName)
      case _ => new ReferenceTypeSymbol(name)
    }
  }

  def splitTypeName(name: String, config: ParserConfig) = {
    if (name.contains('@'))
      (config.targetNamespace, name)
    else if (name.contains(':')) {
      val prefix = name.dropRight(name.length - name.indexOf(':'))
      val value = name.drop(name.indexOf(':') + 1)

      (config.scope.getURI(prefix), value)
    } else
      (config.scope.getURI(null), name)
  }
}

case class SchemaDecl(targetNamespace: String,
    topElems: Map[String, ElemDecl],
    elemList: List[ElemDecl],
    types: Map[String, TypeDecl],
    choices: Set[ChoiceDecl],
    attrs: Map[String, AttributeDecl]) {
  
  val newline = System.getProperty("line.separator")
  
  override def toString: String = {
    "SchemaDecl(topElems(" + topElems.valuesIterator.mkString("," + newline) + "),types(" +
      types.valuesIterator.mkString("," + newline)  + "))"
  }
}

object SchemaDecl {
  def fromXML(node: scala.xml.Node,
      config: ParserConfig = new ParserConfig) = {
    val schema = (node \\ "schema").headOption match {
      case Some(x) => x
      case None    => error("xsd: schema element not found: " + node.toString)
    }
    config.scope = schema.scope
    schema.attribute("targetNamespace") match {
      case Some(x) =>
        config.targetNamespace = x.text
      case None    =>
    }
    
    for (node <- schema \ "element";
        if (node \ "@name").headOption.isDefined) {
      val elem = ElemDecl.fromXML(node, config)
      config.topElems += (elem.name -> elem)
    }
    
    for (node <- schema \\ "complexType";
        if (node \ "@name").headOption.isDefined) {
      val decl = ComplexTypeDecl.fromXML(node, (node \ "@name").text, config)
      config.types += (decl.name -> decl)
    }

    for (node <- schema \\ "simpleType") {
      val decl = SimpleTypeDecl.fromXML(node, config)
      config.types += (decl.name -> decl)
    }
    
    resolveType(config)
    
    SchemaDecl(config.targetNamespace,
      immutable.ListMap.empty[String, ElemDecl] ++ config.topElems,
      config.elemList.toList,
      immutable.ListMap.empty[String, TypeDecl] ++ config.types,
      config.choices,
      immutable.ListMap.empty[String, AttributeDecl] ++ config.attrs)
  }
  
  def resolveType(config: ParserConfig) {    
    for (elem <- config.elemList)
      resolveType(elem.typeSymbol, config)
             
    for (attr <- config.attrs.valuesIterator) {
      attr.typeSymbol match {
        case symbol: BuiltInSimpleTypeSymbol =>
        
        case symbol: ReferenceTypeSymbol =>
          if (!config.containsType(symbol.name))
            error("SchemaDecl: type not found " + attr.name + ": " + symbol.name)
          config.getType(symbol.name) match {
            case decl: SimpleTypeDecl => symbol.decl = decl
            case _ => error("SchemaDecl: type does not match ")
          } // match
      } // match    
    } // for
    
    for (typ <- config.types.valuesIterator) typ match {
      case decl: SimpleTypeDecl => // do nothing
            
      case ComplexTypeDecl(_, SimpleContentDecl(res: SimpContRestrictionDecl), _) =>
        resolveType(res.base, config)
      
      case ComplexTypeDecl(_, SimpleContentDecl(ext: SimpContExtensionDecl), _) =>
        resolveType(ext.base, config)
      
      case ComplexTypeDecl(_, ComplexContentDecl(res: CompContRestrictionDecl), _) =>
        resolveType(res.base, config)
      
      case ComplexTypeDecl(_, ComplexContentDecl(ext: CompContExtensionDecl), _) =>
        resolveType(ext.base, config)
        
      case _ =>
    }
  }
  
  def resolveType(value: XsTypeSymbol, config: ParserConfig): Unit = value match {
    case symbol: ReferenceTypeSymbol =>
      if (!config.containsType(symbol.name))
        error("SchemaDecl: type not found: " + symbol.name)
      
      if (symbol.decl == null)
        symbol.decl = config.getType(symbol.name)
      
    case symbol: BuiltInSimpleTypeSymbol => // do nothing 
    case xsAny => // do nothing
  } // match

}

case class AnnotationDecl() extends Decl

object AnnotationDecl {
  def fromXML(node: scala.xml.Node) = AnnotationDecl() 
}

abstract class AttributeLike extends Decl

object AttributeLike {
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    (node \ "@ref").headOption match {
      case Some(x) => AttributeRef.fromXML(node, config)
      case None => AttributeDecl.fromXML(node, config)
    }
  }
}

case class AttributeRef(namespace: String,
  name: String) extends AttributeLike

object AttributeRef {
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    val ref = (node \ "@ref").text
    val (namespace, typeName) = TypeSymbolParser.splitTypeName(ref, config)
    AttributeRef(namespace, typeName)
  }
}

abstract class AttributeUse
object OptionalUse extends AttributeUse
object ProhibitedUse extends AttributeUse
object RequiredUse extends AttributeUse

case class AttributeDecl(name: String,
    typeSymbol: XsTypeSymbol,
    defaultValue: Option[String],
    fixedValue: Option[String],
    use: AttributeUse) extends AttributeLike {
  override def toString = "@" + name
}

object AttributeDecl {
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    if (!(node \ "@ref").isEmpty) {
      val ref = (node \ "@ref").text      
      config.attrs(ref)
    } else {
      val name = (node \ "@name").text
      var typeSymbol: XsTypeSymbol = xsUnknown
      val typeName = (node \ "@type").text
      if (typeName != "") {
        typeSymbol = TypeSymbolParser.fromString(typeName, config)
      } else {
        for (child <- node.child) child match {
          case <simpleType>{ _* }</simpleType> =>
            typeSymbol = new ReferenceTypeSymbol(SimpleTypeDecl.buildName(child))
          case _ =>
        }
      } // if-else
      
      val defaultValue = (node \ "@default").headOption match {
        case None    => None
        case Some(x) => Some(x.text)
      }
      val fixedValue = (node \ "@fixed").headOption match {
        case None    => None
        case Some(x) => Some(x.text)
      }
      val use = (node \ "@use").text match {
        case "prohibited" => ProhibitedUse
        case "required"   => RequiredUse
        case _            => OptionalUse
      }
      
      val attr = AttributeDecl(name, typeSymbol,
        defaultValue, fixedValue, use)
      config.attrs += (attr.name -> attr)
      attr   
    }
  } 
}

case class ElemRef(namespace: String,
  name: String,
  minOccurs: Option[Int],
  maxOccurs: Option[Int]) extends Decl

object ElemRef {
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    val ref = (node \ "@ref").text   
    val minOccurs = (node \ "@minOccurs").headOption match {
      case None    => None
      case Some(x) => Some(CompositorDecl.buildOccurrence((node \ "@minOccurs").text))
    }
    
    val maxOccurs = (node \ "@maxOccurs").headOption match {
      case None    => None
      case Some(x) => Some(CompositorDecl.buildOccurrence((node \ "@maxOccurs").text))
    }
    
    val (namespace, typeName) = TypeSymbolParser.splitTypeName(ref, config)
    ElemRef(namespace, typeName, minOccurs, maxOccurs)
  }
}

case class ElemDecl(name: String,
  typeSymbol: XsTypeSymbol,
  defaultValue: Option[String],
  fixedValue: Option[String],  
  minOccurs: Int,
  maxOccurs: Int) extends Decl

object ElemDecl {
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    val name = (node \ "@name").text
    var typeSymbol: XsTypeSymbol = xsAny
    val typeName = (node \ "@type").text
    
    if (typeName != "") {
      typeSymbol = TypeSymbolParser.fromString(typeName, config)
    } else {
      for (child <- node.child) child match {
        case <complexType>{ _* }</complexType> =>
          val decl = ComplexTypeDecl.fromXML(child, "complexType@" + name, config)
          config.types += (decl.name -> decl)
          val symbol = new ReferenceTypeSymbol(decl.name)
          symbol.decl = decl
          typeSymbol = symbol
          
        case <simpleType>{ _* }</simpleType> =>
          typeSymbol = new ReferenceTypeSymbol(SimpleTypeDecl.buildName(child))
                      
        case _ =>
      }
    } // if-else
    
    val defaultValue = (node \ "@default").headOption match {
      case None    => None
      case Some(x) => Some(x.text)
    }
    val fixedValue = (node \ "@fixed").headOption match {
      case None    => None
      case Some(x) => Some(x.text)
    }      
    val minOccurs = CompositorDecl.buildOccurrence((node \ "@minOccurs").text)
    val maxOccurs = CompositorDecl.buildOccurrence((node \ "@maxOccurs").text)
    
    val elem = ElemDecl(name, typeSymbol, defaultValue, fixedValue, minOccurs, maxOccurs)
    config.elemList += elem
    elem
  }
}


abstract class TypeDecl extends Decl

/** simple types cannot have element children or attributes.
 */
case class SimpleTypeDecl(name: String, content: ContentTypeDecl) extends TypeDecl {
  override def toString(): String = name + "(" + content.toString + ")"
}

object SimpleTypeDecl {
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    val name = buildName(node)
    
    var content: ContentTypeDecl = null
    for (child <- node.child) child match {
      case <restriction>{ _* }</restriction>  => content = SimpTypRestrictionDecl.fromXML(child, config) 
      case <list>{ _* }</list>                => content = SimpTypListDecl.fromXML(child, config)
      case <union>{ _* }</union>              => content = SimpTypUnionDecl.fromXML(child, config)
      case _ =>     
    }
    
    SimpleTypeDecl(name, content)
  }
  
  def buildName(node: scala.xml.Node) = {
    val name = (node \ "@name").text
    if (name != "")
      name
    else
      "simpleType@" + node.hashCode.toString
  }
}

/** complex types may have element children and attributes.
 */
case class ComplexTypeDecl(name: String,
  content: HasComplexTypeContent,
  attributes: List[AttributeLike]) extends TypeDecl

object ComplexTypeDecl {  
  def fromXML(node: scala.xml.Node, name: String, config: ParserConfig) = {
    var content: HasComplexTypeContent = ComplexContentDecl.empty
    
    val attributes = (node \ "attribute").toList.map(
      AttributeLike.fromXML(_, config))
    
    for (child <- node.child) child match {
      case <group>{ _* }</group> =>
        error("Unsupported content type: " + child.toString)
      case <all>{ _* }</all> =>
        content = ComplexContentDecl.fromCompositor(
          AllDecl.fromXML(child, config), attributes)
      case <choice>{ _* }</choice> =>
        content = ComplexContentDecl.fromCompositor(
          ChoiceDecl.fromXML(child, config), attributes)
      case <sequence>{ _* }</sequence> =>
        content = ComplexContentDecl.fromCompositor(
          SequenceDecl.fromXML(child, config), attributes)
      case <simpleContent>{ _* }</simpleContent> =>
        content = SimpleContentDecl.fromXML(child, config)
      case <complexContent>{ _* }</complexContent> =>
        content = ComplexContentDecl.fromXML(child, config)
      case _ =>
    }
    
    // val contentModel = ContentModel.fromSchema(firstChild(node))
    ComplexTypeDecl(name, content, attributes.reverse)
  }
}

trait HasComplexTypeContent {
  val content: ComplexTypeContent
}

trait HasContent {
  val content: ContentTypeDecl
}

/** complex types with simple content only allow character content.
 */
case class SimpleContentDecl(content: ComplexTypeContent) extends Decl with HasComplexTypeContent

object SimpleContentDecl {
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    var content: ComplexTypeContent = null
    
    for (child <- node.child) child match {
      case <restriction>{ _* }</restriction> =>
        content = SimpContRestrictionDecl.fromXML(child, config)
      case <extension>{ _* }</extension> =>
        content = SimpContExtensionDecl.fromXML(child, config)
      case _ =>
    }
       
    SimpleContentDecl(content)
  }
}

/** only complex types with complex content allow child elements
 */
case class ComplexContentDecl(content: ComplexTypeContent) extends Decl with HasComplexTypeContent

object ComplexContentDecl {
  lazy val empty =
    ComplexContentDecl(CompContRestrictionDecl.empty)
  
  def fromCompositor(compositor: HasParticle, attributes: List[AttributeLike]) =
    ComplexContentDecl(CompContRestrictionDecl.fromCompositor(compositor, attributes))
  
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    var content: ComplexTypeContent = CompContRestrictionDecl.empty
    
    for (child <- node.child) child match {
      case <restriction>{ _* }</restriction> =>
        content = CompContRestrictionDecl.fromXML(child, config)
      case <extension>{ _* }</extension> =>
        content = CompContExtensionDecl.fromXML(child, config)
      case _ =>
    }
    
    ComplexContentDecl(content)
  }
}

abstract class CompositorDecl extends Decl

object CompositorDecl {
  def fromNodeSeq(seq: scala.xml.NodeSeq, config: ParserConfig) = {
    for (child <- seq.toList;
        if (child.isInstanceOf[scala.xml.Elem]) && (child.label != "attribute"))
      yield fromXML(child, config)
  }
  
  def fromXML(node: scala.xml.Node, config: ParserConfig): Decl = node match {
    case <element>{ _* }</element>   =>
      if ((node \ "@name").headOption.isDefined)
        ElemDecl.fromXML(node, config)
      else if ((node \ "@ref").headOption.isDefined)
        ElemRef.fromXML(node, config)
      else
        error("xsd: Unspported content type " + node.toString) 
    case <choice>{ _* }</choice>     => ChoiceDecl.fromXML(node, config)
    case <sequence>{ _* }</sequence> => SequenceDecl.fromXML(node, config)
    case <all>{ _* }</all>           => AllDecl.fromXML(node, config)
    
    case _ => error("xsd: Unspported content type " + node.label)   
  }
  
  def buildOccurrence(value: String) =
    if (value == "")
      1
    else if (value == "unbounded")
      Integer.MAX_VALUE
    else
      value.toInt
}

trait HasParticle {
  val particles: List[Decl]
  val minOccurs: Int
  val maxOccurs: Int
}

case class SequenceDecl(particles: List[Decl],
  minOccurs: Int,
  maxOccurs: Int) extends CompositorDecl with HasParticle

object SequenceDecl {
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    val minOccurs = CompositorDecl.buildOccurrence((node \ "@minOccurs").text)
    val maxOccurs = CompositorDecl.buildOccurrence((node \ "@maxOccurs").text)
    SequenceDecl(CompositorDecl.fromNodeSeq(node.child, config), minOccurs, maxOccurs)
  }
}

case class ChoiceDecl(particles: List[Decl],
  minOccurs: Int,
  maxOccurs: Int) extends CompositorDecl with HasParticle

object ChoiceDecl {
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    val minOccurs = CompositorDecl.buildOccurrence((node \ "@minOccurs").text)
    val maxOccurs = CompositorDecl.buildOccurrence((node \ "@maxOccurs").text)
    val choice = ChoiceDecl(CompositorDecl.fromNodeSeq(node.child, config), minOccurs, maxOccurs)
    config.choices += choice
    choice
  }
}

case class AllDecl(particles: List[Decl],
  minOccurs: Int,
  maxOccurs: Int) extends CompositorDecl with HasParticle

object AllDecl {
  def fromXML(node: scala.xml.Node, config: ParserConfig) = {
    val minOccurs = CompositorDecl.buildOccurrence((node \ "@minOccurs").text)
    val maxOccurs = CompositorDecl.buildOccurrence((node \ "@maxOccurs").text)
    AllDecl(CompositorDecl.fromNodeSeq(node.child, config), minOccurs, maxOccurs)
  }
}
