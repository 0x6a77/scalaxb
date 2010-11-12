/**
 * @author  e.e d3si9n
 */

import scalaxb._
import Scalaxb._
import general._
import DefaultXMLProtocol._

object GeneralUsage {
  val NS = Some("http://www.example.com/general")
  
  def main(args: Array[String]) = {
    allTests
  }

  def allTests = {
    testSingularBuiltInType
    testSingularSimpleType
    testList
    testSingularComplexType
    testChoiceComplexType
    testAny
    true
  }
  
  def testSingularBuiltInType {
    val subject = <foo xmlns="http://www.example.com/general"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <int>1</int>
      <byte>1</byte>
      <short>1</short>
      <long>1</long>
      <float>1.0</float>
      <double>1.0</double>
      <integer>1</integer>
      <nonPositiveInteger>-1</nonPositiveInteger>
      <negativeInteger>-1</negativeInteger>
      <nonNegativeInteger>1</nonNegativeInteger>
      <positiveInteger>1</positiveInteger>
      <unsignedLong>1</unsignedLong>
      <unsignedInt>1</unsignedInt>
      <unsignedShort>1</unsignedShort>
      <unsignedByte>1</unsignedByte>
      <decimal>1</decimal>
      <boolean>false</boolean>
      <string>foo</string>
      <normalizedString>foo</normalizedString>
      <token>foo</token>
      <language>en-US</language>
      <Name>foo</Name>
      <NCName>foo</NCName>
      <NMTOKEN>foo</NMTOKEN>
      <NMTOKENS>foo</NMTOKENS>
      <ID>foo</ID>
      <IDREF>foo</IDREF>
      <IDREFS>foo</IDREFS>
      <ENTITY>foo</ENTITY>
      <ENTITIES>foo</ENTITIES>
    </foo>
    
    val obj = fromXML[SingularBuiltInTypeTest](subject)
    
    val BI = BigInt(1)
    val BD = BigDecimal(1)
    def check(obj: Any) = obj match {
        case SingularBuiltInTypeTest(
          SingularBuiltInTypeTestSequence1(1, 1, 1, 1, 1.0F, 1.0, 1, -1, -1, 1),
          SingularBuiltInTypeTestSequence2(1, BI, 1, 1, 1, BD, false, "foo", "foo", "foo"),
          SingularBuiltInTypeTestSequence3("en-US", "foo", "foo", "foo",  Array("foo"),
            "foo", "foo", Array("foo"), "foo", Array("foo"))
          ) =>
        case _ => error("match failed: " + obj.toString)
      }
    check(obj)
    val document = toXML[SingularBuiltInTypeTest](obj, None, Some("foo"), subject.scope)
    check(fromXML[SingularBuiltInTypeTest](document))
    println(document)
  }
      
  def testSingularSimpleType {
    val subject = <foo xmlns="http://www.example.com/general"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <number1>1</number1>
      <number2 xsi:nil="true"/>
      <number4>1</number4>
      <number5>2</number5><number5>1</number5>
      
      <milk1>WHOLE</milk1>
      <milk2 xsi:nil="true"/>
      <milk5>WHOLE</milk5><milk5>SKIM</milk5>
    </foo>
    val obj = fromXML[SingularSimpleTypeTest](subject)
    
    def check(obj: Any) = obj match {
        case SingularSimpleTypeTest(1, None, None, Some(Some(1)), Seq(2, 1), Seq(), 
          WHOLE, None, None, None, Seq(WHOLE, SKIM), Seq(),
          None, None) =>
        case _ => error("match failed: " + obj.toString)
      }
    check(obj)
    val document = toXML[SingularSimpleTypeTest](obj, None, Some("foo"), subject.scope)
    check(fromXML[SingularSimpleTypeTest](document))
    println(document)
  }
  
  def testList {
    val subject = <foo xmlns="http://www.example.com/general"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <numbers1>1 2 3</numbers1>
      <numbers2 xsi:nil="true"/>
      <numbers4>1</numbers4>
      <numbers5></numbers5><numbers5>1</numbers5>
      <numbers6 xsi:nil="true"/>
      
      <milk1>WHOLE</milk1>
      <milk2 xsi:nil="true"/>
      <milk5></milk5><milk5>SKIM</milk5>
      <milk6 xsi:nil="true"/>
    </foo>
    val obj = fromXML[ListTest](subject)
    
    def check(obj: Any) = obj match {
        case ListTest(Seq(1, 2, 3), None, None, Some(Some(Seq(1))), Seq(Seq(), Seq(1)), Seq(None), 
          Seq(WHOLE), None, None, None, Seq(Seq(), Seq(SKIM)), Seq(None), 
          None, None) =>
        case _ => error("match failed: " + obj.toString)
      }
    check(obj)
    val document = toXML[ListTest](obj, None, Some("foo"), subject.scope)
    check(fromXML[ListTest](document))
    println(document)
  }
  
  def testSingularComplexType {
    val subject = <foo xmlns="http://www.example.com/general"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <person1><firstName>John</firstName><lastName>Doe</lastName></person1>
      <person2 xsi:nil="true"/>
      <person3><firstName>John</firstName><lastName>Doe</lastName></person3>
      <person5><firstName>John</firstName><lastName>Doe</lastName></person5>
        <person5><firstName>John</firstName><lastName>Doe</lastName></person5>
      <person6 xsi:nil="true"/>
    </foo>
    val obj = fromXML[SingularComplexTypeTest](subject)
    
    def check(obj: Any) = obj match {
        case SingularComplexTypeTest(Person("John", "Doe"), None, Some(Person("John", "Doe")), None,
          Seq(Person("John", "Doe"), Person("John", "Doe")),
          Seq(None)) =>
        case _ => error("match failed: " + obj.toString)
      }
    
    check(obj)
    val document = toXML[SingularComplexTypeTest](obj, None, Some("foo"), subject.scope)
    check(fromXML[SingularComplexTypeTest](document))
    println(document)
  }
  
  def testChoiceComplexType {
    val subject = <foo xmlns="http://www.example.com/general"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <person1><firstName>John</firstName><lastName>Doe</lastName></person1>
      <person2 xsi:nil="true"/>
      <person3><firstName>John</firstName><lastName>Doe</lastName></person3>
      <person5><firstName>John</firstName><lastName>Doe</lastName></person5>
        <person5><firstName>John</firstName><lastName>Doe</lastName></person5>
      <person6 xsi:nil="true"/>
    </foo>
    val obj = fromXML[ChoiceComplexTypeTest](subject)
    
    def check(obj: Any) = obj match {
        case ChoiceComplexTypeTest(
          DataRecord(NS, Some("person1"), Person("John", "Doe")),
          DataRecord(NS, Some("person2"), None),
          Some(DataRecord(NS, Some("person3"), Person("John", "Doe") )),
          None,
          Seq(DataRecord(NS, Some("person5"), Person("John", "Doe")),
            DataRecord(NS, Some("person5"), Person("John", "Doe"))),
          Seq(DataRecord(NS, Some("person6"), None)) ) =>
        case _ => error("match failed: " + obj.toString)
      }
    
    check(obj)
    val document = toXML[ChoiceComplexTypeTest](obj, None, Some("foo"), subject.scope)
    check(fromXML[ChoiceComplexTypeTest](document))
    println(document)
  }
  
  /*
  <xs:complexType name="AnyTest">
    <xs:sequence>
      <xs:element name="person1" type="gen:Person"/>
      <xs:any namespace="##other" processContents="lax"/>
      <xs:choice>
        <xs:element name="person2" nillable="true" type="gen:Person"/>
        <xs:any namespace="##other" processContents="lax"/>
      </xs:choice>
      <xs:element name="person3" minOccurs="0" type="gen:Person"/>
      <xs:any namespace="##other" processContents="lax" minOccurs="0"/>
      <xs:choice>
        <xs:element name="person4" minOccurs="0" nillable="true" type="gen:Person"/>
        <xs:any namespace="##other" processContents="lax" minOccurs="0"/>
      </xs:choice>
      <xs:element name="person5" maxOccurs="unbounded" type="gen:Person"/>
      <xs:any namespace="##other" processContents="lax" maxOccurs="unbounded"/>
    </xs:sequence>
  </xs:complexType>
  */
  def testAny {
    val subject = <foo xmlns="http://www.example.com/general"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:xs="http://www.w3.org/2001/XMLSchema">
      <person1><firstName>John</firstName><lastName>Doe</lastName></person1>
      <foo xsi:type="xs:int">1</foo>
      <person2 xsi:nil="true"/>
      <person3><firstName>John</firstName><lastName>Doe</lastName></person3>
      <foo xsi:type="xs:int">1</foo>
      <foo xsi:type="xs:int">1</foo>
      <person5><firstName>John</firstName><lastName>Doe</lastName></person5>
        <person5><firstName>John</firstName><lastName>Doe</lastName></person5>
      <foo xsi:type="xs:int">1</foo><foo xsi:type="xs:int">1</foo>
    </foo>
    val obj = fromXML[AnyTest](subject)
    
    def check(obj: Any) = obj match {
        case AnyTest(
          Person("John", "Doe"),
          DataRecord(NS, Some("foo"), 1), // Single
          DataRecord(NS, Some("person2"), None),
          Some(Person("John", "Doe")),
          Some(DataRecord(NS, Some("foo"), 1)), // optional
          Some(DataRecord(NS, Some("foo"), Some(1))), // nillable optional
          Seq(Person("John", "Doe"), Person("John", "Doe")),
          Seq(DataRecord(NS, Some("foo"), 1), // multiple
            DataRecord(NS, Some("foo"), 1))
           ) =>
        case _ => error("match failed: " + obj.toString)
      }
    check(obj)
    val document = toXML[AnyTest](obj, None, Some("foo"), subject.scope)
    check(fromXML[AnyTest](document))
    println(document)
  }
}
