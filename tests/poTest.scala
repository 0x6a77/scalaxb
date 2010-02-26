/**
 * @author  e.e d3si9n
 */

import purchaseorder._

object XsdTest {
  def main(args: Array[String]) = {
    testUSAddress
    testItem
    testPurchaseOrder
    testTimeOlson
    testIntWithAttr
  }
  
  def testUSAddress {
    val subject = <USAddress>
      <name>Foo</name>
      <street>1537 Paper Street</street>
      <city>Wilmington</city>
      <state>DE</state>
      <zip>19808</zip>
    </USAddress>
    
    val address = USAddress.fromXML(subject)
    address match {
      case USAddress("Foo",
        "1537 Paper Street",
        "Wilmington",
        "DE",
        19808) =>
      case _ => throw new Exception("match failed: " + address.toString)
    }
    
    println(address.toString)
  }
  
  def testItem {
    val subject = <item partNum="639-OS">
      <productName>Olive Soap</productName>
      <quantity>1</quantity>
      <USPrice>4.00</USPrice>
      <shipDate>2010-02-06Z</shipDate>
    </item>
    
    val item = Item.fromXML(subject)
    item match {
      case Item("Olive Soap",
        1,
        usPrice,
        None,
        Some(Calendar("2010-02-06T00:00:00.000Z")),
        "639-OS") =>
          if (usPrice != BigDecimal(4.00))
            throw new Exception("values don't match: " + item.toString)
      case _ => throw new Exception("match failed: " + item.toString)
    }
    
    println(item.toString)
  }
  
  def testPurchaseOrder {
    val subject = <purchaseOrder
        xmlns="http://www.example.com/IPO"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:ipo="http://www.example.com/IPO"
        orderDate="1999-12-01Z">
      <shipTo exportCode="1" xsi:type="ipo:UKAddress">
        <name>Helen Zoe</name>
        <street>47 Eden Street</street>
        <city>Cambridge</city>
        <postcode>CB1 1JR</postcode>
      </shipTo>
      <billTo xsi:type="ipo:USAddress">
        <name>Foo</name>
        <street>1537 Paper Street</street>
        <city>Wilmington</city>
        <state>DE</state>
        <zip>19808</zip>   
      </billTo>
      <items>
        <item partNum="639-OS">
          <productName>Olive Soap</productName>
          <quantity>1</quantity>
          <USPrice>4.00</USPrice>
          <shipDate>2010-02-06Z</shipDate>
        </item>
      </items>
    </purchaseOrder>
    
    val purchaseOrder = PurchaseOrderType.fromXML(subject)
    purchaseOrder match {
      case PurchaseOrderType(
        shipTo: UKAddress,
        billTo: USAddress,
        None,
        Items(_),
        Some(Calendar("1999-12-01T00:00:00.000Z"))) =>
      case _ => throw new Exception("match failed: " + purchaseOrder.toString)
    }    
    println(purchaseOrder.toString)  
  }
  
  def testTimeOlson {
    val subject = <time>00:00:00.000Z</time>
    
    val timeOlson = TimeOlson.fromXML(subject)
    timeOlson match {
      case TimeOlson(Calendar("1970-01-01T00:00:00.000Z"),
        "") =>
      case _ => throw new Exception("match failed: " + timeOlson.toString)
    }
    
    println(timeOlson.toString)
  }
  
  def testIntWithAttr {
    val subject = <some foo="test">1</some>
    
    val intWithAttr = IntWithAttr.fromXML(subject)
    intWithAttr match {
      case IntWithAttr(1, "test") =>
      case _ => throw new Exception("match failed: " + intWithAttr.toString)
    }
    
    println(intWithAttr.toString)    
  }
}
