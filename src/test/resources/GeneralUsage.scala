/**
 * @author  e.e d3si9n
 */

import scalaxb._
import Scalaxb._
import general._
import DefaultXMLProtocol._

object GeneralUsage {
  def main(args: Array[String]) = {
    allTests
  }

  def allTests = {
    testSingularSimpleType
    testList
    testSingularComplexType
    true
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
    obj match {
      case SingularSimpleTypeTest(1, None, None, Some(1), Seq(2, 1), Seq(), 
        WHOLE, None, None, None, Seq(WHOLE, SKIM), Seq(),
        None, None) =>
      case _ => error("match failed: " + obj.toString)
    }
    val document = toXML[SingularSimpleTypeTest](obj, None, Some("foo"), subject.scope)
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
    obj match {
      case ListTest(Seq(1, 2, 3), None, None, Some(Seq(1)), Seq(Seq(), Seq(1)), Seq(None), 
        Seq(WHOLE), None, None, None, Seq(Seq(), Seq(SKIM)), Seq(None), 
        None, None) =>
      case _ => error("match failed: " + obj.toString)
    }
    val document = toXML[ListTest](obj, None, Some("foo"), subject.scope)
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
    obj match {
      case SingularComplexTypeTest(Person("John", "Doe"), None, Some(Person("John", "Doe")), None,
        Seq(Person("John", "Doe"), Person("John", "Doe")),
        Seq(None)) =>
      case _ => error("match failed: " + obj.toString)
    }
    val document = toXML[SingularComplexTypeTest](obj, None, Some("foo"), subject.scope)
    println(document)
  }
}
