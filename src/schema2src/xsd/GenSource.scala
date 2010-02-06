/**
 * @author  e.e d3si9n
 */

package schema2src.xsd

import schema2src._
// import scala.xml.xsd.{ ElemDecl, TypeDecl, XsTypeSymbol }
import scala.collection.Map
import scala.xml._

class GenSource(conf: Driver.XsdConfig, schema: (Map[String, ElemDecl], Map[String, TypeDecl])) extends ScalaNames {
  import conf.{outfile => fOut, objName => objectName}
  
  val elems = schema._1
  val typs = schema._2
  val newline = System.getProperty("line.separator")
  
  def makePackageName = {
    <source>package {conf.packageName}
</source>
  }
  
  def makeParentClass = {
    <source>
abstract class DataModel
</source>    
  }
  
  def makeHelperObject = {
    <source>
object Helper {{
  lazy val typeFactory = javax.xml.datatype.DatatypeFactory.newInstance()
  
  def toCalendar(value: String) =
    typeFactory.newXMLGregorianCalendar(value).toGregorianCalendar

  def toDuration(value: String) =
    typeFactory.newDuration(value)
}}
</source>    
  }
      
  def buildValueCode(decl: SimpleTypeDecl, nodeName: String): String = decl.content match {  
    case restriction: RestrictionDecl =>
      restriction.base match {
        case builtIn: BuiltInSimpleTypeSymbol => buildValueCode(builtIn, nodeName)
        case _ => throw new Exception("GenSource: Unsupported type " + restriction.base.toString) 
      }
    case _ => throw new Exception("GenSource: Unsupported content " + decl.content.toString)    
  }
    
  def buildValueCode(typeSymbol: BuiltInSimpleTypeSymbol, nodeName: String): String = {
    val stringValueCode = "(node \\ \"" + nodeName + "\").text"
    typeSymbol.name match {
      case "String"     => stringValueCode
      case "javax.xml.datatype.Duration" => "Helper.toDuration(" + stringValueCode + ")"
      case "java.util.Calendar" => "Helper.toCalendar(" + stringValueCode + ")"
      case "Boolean"    => stringValueCode + ".toBoolean"
      case "Int"        => stringValueCode + ".toInt"
      case "Long"       => stringValueCode + ".toLong"
      case "Short"      => stringValueCode + ".toShort"
      case "Float"      => stringValueCode + ".toFloat"
      case "Double"     => stringValueCode + ".toDouble"
      case "Byte"       => stringValueCode + ".toByte"
      case "BigInt"     => "BigInt(" + stringValueCode + ")"
      case "BigDecimal" => "BigDecimal(" + stringValueCode + ")"
      case "java.net.URI" => "java.net.URI.create(" + stringValueCode + ")"
      case "javax.xml.namespace.QName"
        => "javax.xml.namespace.QName.valueOf(" + stringValueCode + ")"
      case "Array[String]" => stringValueCode + ".split(' ')"
      // case "Base64Binary" =>
      // case "HexBinary"  => 
      case _        => throw new Exception("GenSource: Unsupported type " + typeSymbol.toString) 
    }        
  }
    
  def makeIndent(indent: Int) =
    "  " * indent
     
  def makeCaseClassWithType(name: String, complexTypeDecl: ComplexTypeDecl): scala.xml.Node = {
    val childElements = flattenElements(complexTypeDecl.compositor)
    val list = List.concat[Decl](complexTypeDecl.attributes, childElements)
    val paramList = buildParamList(list)
    val argList = buildArgList(list)
    return <source>
case class {name}(
  {paramList.mkString("," + newline + makeIndent(1))}) extends DataModel {{
}}

object {name} {{
  def fromXML(node: scala.xml.Node) =
    {name}({argList.mkString("," + newline + makeIndent(3))}) 
  
  def fromSequence(sequence: scala.xml.NodeSeq) =
    for (node &lt;- sequence.toList)
      yield fromXML(node)
}}
</source>    
  }
  
  def buildParamList(decls: List[Decl]) =
    for (decl <- decls)
      yield buildParam(decl)
  
  def buildParam(decl: Decl) = decl match {
    case elem: ElemDecl =>
      if (elem.maxOccurs == 0 || elem.maxOccurs == 1) {
        elem.name + ": " + buildTypeName(elem.typeSymbol, elem.name)
      } else {
        elem.name + ": List[" + buildTypeName(elem.typeSymbol, elem.name) + "]"
      }
    case attr: AttributeDecl =>
      attr.name + ": " + buildTypeName(attr.typeSymbol, attr.name)
    case _ => throw new Exception("GenSource: unsupported delcaration " + decl.toString)
  }
    
  def buildArgList(decls: List[Decl]) =
    for (decl <- decls)
      yield buildArg(decl)
  
  def buildArg(decl: Decl): String = decl match {
    case elem: ElemDecl       => buildArg(elem)
    case attr: AttributeDecl  => buildArg(attr)
    case _ => throw new Exception("GenSource: unsupported delcaration " + decl.toString)
  }
  
  def buildArg(elem: ElemDecl, decl: ComplexTypeDecl): String = {
    val typeName = buildTypeName(elem.typeSymbol, elem.name)
    
    if (elem.maxOccurs == 0 || elem.maxOccurs == 1) {
      typeName + ".fromXML((node \\ \"" + elem.name + "\").head)" 
    } else {
      typeName + ".fromSequence(node \\ \"" + elem.name + "\")" 
    }    
  }
  
  def buildArg(elem: ElemDecl): String = {
    val typeSymbol = elem.typeSymbol
    
    typeSymbol match {
      case symbol: BuiltInSimpleTypeSymbol => buildValueCode(symbol, elem.name)
      case symbol: SimpleTypeSymbol  => buildValueCode(symbol.decl, elem.name)
      case symbol: ComplexTypeSymbol => buildArg(elem, symbol.decl)  
      case symbol: ReferenceTypeSymbol =>
        symbol.decl match {
          case decl: SimpleTypeDecl   => buildValueCode(decl, elem.name)
          case decl: ComplexTypeDecl  => buildArg(elem, decl)
          case _ => throw new Exception("GenSource: Invalid type " + symbol.decl.toString)
        }
      case _ => throw new Exception("GenSource: Invalid type " + typeSymbol.toString)
    }
  }
  
  def buildArg(attr: AttributeDecl): String = attr.typeSymbol match {
    case symbol: BuiltInSimpleTypeSymbol => buildValueCode(symbol, "@" + attr.name)   
    case symbol: SimpleTypeSymbol        => buildValueCode(symbol.decl, "@" + attr.name)
  }
  
  def buildTypeName(content: SimpleTypeContent): String = content match {
    case restriction: RestrictionDecl => restriction.base.name
    case _ => throw new Exception("GenSource: Unsupported content " + content.toString)
  }
  
  def buildTypeName(typeSymbol: XsTypeSymbol, elemName: String): String = typeSymbol match {
    case symbol: BuiltInSimpleTypeSymbol => symbol.name
    case symbol: SimpleTypeSymbol  => buildTypeName(symbol.decl.content)
    case symbol: ComplexTypeSymbol =>
      if (!symbol.name.contains("@"))
        makeTypeName(symbol.name)
      else
        makeTypeName(elemName)
    case symbol: ReferenceTypeSymbol =>
      symbol.decl match {
        case decl: SimpleTypeDecl   => buildTypeName(decl.content)
        case decl: ComplexTypeDecl  =>
          if (!symbol.name.contains("@"))
            makeTypeName(symbol.name)
          else
            makeTypeName(elemName)        
        
        case _ => throw new Exception("GenSource: Invalid type " + typeSymbol.toString)
      }
    case _ => throw new Exception("GenSource: Invalid type " + typeSymbol.toString)    
  }
    
  def flattenElements(compositor: HasParticle): List[ElemDecl] = {
    val list = for (particle <- compositor.particles)
      yield particle match {
        case compositor2: HasParticle => flattenElements(compositor2)
        case elem: ElemDecl           => List(elem)
        case _ => throw new Exception("GenSource: Invalid content model" + particle.toString)
      }
    list.flatten
  }
  
  def makeElement(decl: ElemDecl): scala.xml.Node = {
    val complexTypeSymbol = decl.typeSymbol match {
      case sym: ComplexTypeSymbol => sym
      case _ => throw new Exception("GenSource: ComplexTypeSymbol is required.")
    }
    val complexTypeDecl = complexTypeSymbol.decl
    val typeName = makeTypeName(decl.name)
    
    makeCaseClassWithType(typeName, complexTypeDecl)
  }
  
  def makeType(decl: ComplexTypeDecl): scala.xml.Node =
    makeCaseClassWithType(makeTypeName(decl.name), decl)
    
  def makeTypeName(name: String) =
    if (name.contains("."))
      name
    else
      name.substring(0, 1).toUpperCase() + name.substring(1)

  def myprintAll(nodes: Seq[Node]) {
    for (node <- nodes)
      myprint(node)
  }
  
  def myprint(n: Node) = n match {
    case Text(s)          => fOut.print(s)
    case EntityRef("lt")  => fOut.print('<')
    case EntityRef("gt")  => fOut.print('>')
    case EntityRef("amp") => fOut.print('&')
    case atom: Atom[_]    => fOut.print(atom.text)
    case _                => Main.log("error in xsd:run: encountered "
      + n.getClass() + " " + n.toString)
  }
  
  def run {
    import scala.collection.mutable
    Main.log("xsd: GenSource.run")
    
    if (conf.packageName != null)
      myprintAll(makePackageName.child)
    
    myprintAll(makeParentClass.child)
    
    for (elemPair <- elems;
        if elemPair._2.typeSymbol.isInstanceOf[ComplexTypeSymbol];
        if elemPair._2.typeSymbol.name.contains("@"))
      myprintAll(makeElement(elemPair._2).child)
    
    for (typePair <- typs;
        if typePair._2.isInstanceOf[ComplexTypeDecl] && !typePair._1.contains("@") )
      myprintAll(makeType(typePair._2.asInstanceOf[ComplexTypeDecl]).child)
      
    myprintAll(makeHelperObject.child)
  }
}
