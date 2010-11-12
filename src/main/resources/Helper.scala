package scalaxb

import scala.xml.{Node, NodeSeq, NamespaceBinding, Elem, PrefixedAttribute}
import javax.xml.datatype.{XMLGregorianCalendar}

object Scalaxb {
  def fromXML[A](seq: NodeSeq)(implicit format: XMLFormat[A]): A = format.readsXML(seq)
  def toXML[A](__obj: A, __namespace: Option[String], __elementLabel: Option[String],
      __scope: NamespaceBinding, __typeAttribute: Boolean = false)(implicit format: CanWriteXML[A]): NodeSeq =
    format.writesXML(__obj, __namespace, __elementLabel, __scope, __typeAttribute)
}

trait XMLFormat[A] extends CanWriteXML[A] with CanReadXML[A]

trait CanReadXML[A] {
  def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, A]
  
  def readsXML(seq: scala.xml.NodeSeq): A = readsXMLEither(seq) match {
    case Right(a) => a
    case Left(a) => error(a)
  }
}

trait CanWriteXML[A] {
  def writesXML(__obj: A, __namespace: Option[String], __elementLabel: Option[String],
      __scope: NamespaceBinding, __typeAttribute: Boolean): NodeSeq
}

object XMLFormat {
  implicit object __NodeXMLWriter extends CanWriteXML[Node] {
    def writesXML(__obj: Node, __namespace: Option[String], __elementLabel: Option[String],
      __scope: NamespaceBinding, __typeAttribute: Boolean): NodeSeq = __obj
  }

  implicit object __NodeSeqXMLWriter extends CanWriteXML[NodeSeq] {
    def writesXML(__obj: NodeSeq, __namespace: Option[String], __elementLabel: Option[String],
      __scope: NamespaceBinding, __typeAttribute: Boolean): NodeSeq = __obj
  }
  
  implicit object __ElemXMLWriter extends CanWriteXML[Elem] {
    def writesXML(__obj: Elem, __namespace: Option[String], __elementLabel: Option[String],
      __scope: NamespaceBinding, __typeAttribute: Boolean): NodeSeq = __obj
  }  

  implicit object __StringXMLFormat extends XMLFormat[String] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, String] = Right(seq.text)
    
    def writesXML(__obj: String, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj, None, __namespace, __elementLabel, __scope)
  }
  
  object XSStringXMLWriter extends CanWriteXML[String] {
    def writesXML(__obj: String, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj, Some("string"), __namespace, __elementLabel, __scope)
  }
  
  def stringXMLWriter(xstype: String) = new CanWriteXML[String] {
    def writesXML(__obj: String, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj, Some(xstype), __namespace, __elementLabel, __scope)
  }

  implicit object __IntXMLFormat extends XMLFormat[Int] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, Int] = try { Right(seq.text.toInt) }
      catch { case e: Exception => Left(e.toString) }
    
    def writesXML(__obj: Int, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  object XSIntXMLWriter extends CanWriteXML[Int] {    
    def writesXML(__obj: Int, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some("int"), __namespace, __elementLabel, __scope)
  }
  
  implicit object __ByteXMLFormat extends XMLFormat[Byte] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, Byte] = try { Right(seq.text.toByte) }
      catch { case e: Exception => Left(e.toString) }
    
    def writesXML(__obj: Byte, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  object XSByteXMLWriter extends CanWriteXML[Byte] {
    def writesXML(__obj: Byte, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some("byte"), __namespace, __elementLabel, __scope)
  }
  
  implicit object __ShortXMLFormat extends XMLFormat[Short] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, Short] = try { Right(seq.text.toShort) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: Short, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  object XSShortXMLWriter extends CanWriteXML[Short] {
    def writesXML(__obj: Short, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some("short"), __namespace, __elementLabel, __scope)
  }
  
  implicit object __LongXMLFormat extends XMLFormat[Long] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, Long] = try { Right(seq.text.toLong) }
      catch { case e: Exception => Left(e.toString) }
    
    def writesXML(__obj: Long, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  def longXMLWriter(xstype: String) = new CanWriteXML[Long] {
    def writesXML(__obj: Long, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some(xstype), __namespace, __elementLabel, __scope)
  }
  
  implicit object __BigDecimalXMLFormat extends XMLFormat[BigDecimal] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, BigDecimal] = try { Right(BigDecimal(seq.text)) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: BigDecimal, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  object XSDecimalXMLWriter extends CanWriteXML[BigDecimal] {
    def writesXML(__obj: BigDecimal, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some("decimal"), __namespace, __elementLabel, __scope)
  }
  
  implicit object __BigIntXMLFormat extends XMLFormat[BigInt] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, BigInt] = try { Right(BigInt(seq.text)) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: BigInt, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  def bigIntXMLWriter(xstype: String) = new CanWriteXML[BigInt] {
    def writesXML(__obj: BigInt, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some(xstype), __namespace, __elementLabel, __scope)
  }
  
  def intXMLWriter(xstype: String) = new CanWriteXML[Int] {
    def writesXML(__obj: Int, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some(xstype), __namespace, __elementLabel, __scope)
  }
  
  implicit object __FloatXMLFormat extends XMLFormat[Float] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, Float] = try { Right(seq.text.toFloat) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: Float, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  object XSFloatXMLWriter extends CanWriteXML[Float] {
    def writesXML(__obj: Float, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some("float"), __namespace, __elementLabel, __scope)
  }
  
  implicit object __DoubleXMLFormat extends XMLFormat[Double] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, Double] = try { Right(seq.text.toDouble) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: Double, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  object XSDoubleXMLWriter extends CanWriteXML[Double] {
    def writesXML(__obj: Double, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some("double"), __namespace, __elementLabel, __scope)
  }
  
  implicit object __BooleanXMLFormat extends XMLFormat[Boolean] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, Boolean] = try { Right(seq.text.toBoolean) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: Boolean, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  object XSBooleanXMLWriter extends CanWriteXML[Boolean] {
    def writesXML(__obj: Boolean, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some("boolean"), __namespace, __elementLabel, __scope)
  }
  
  implicit object __DurationXMLFormat extends XMLFormat[javax.xml.datatype.Duration] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, javax.xml.datatype.Duration] = try { Right(Helper.toDuration(seq.text)) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: javax.xml.datatype.Duration, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  object XSDurationXMLWriter extends CanWriteXML[javax.xml.datatype.Duration] {
    def writesXML(__obj: javax.xml.datatype.Duration, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some("duration"), __namespace, __elementLabel, __scope)
  }
  
  implicit object __CalendarXMLFormat extends XMLFormat[XMLGregorianCalendar] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, XMLGregorianCalendar] = try { Right(XMLCalendar(seq.text)) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: XMLGregorianCalendar, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toXMLFormat, None, __namespace, __elementLabel, __scope)
  }
  
  def calendarXMLWriter(xstype: String) = new CanWriteXML[XMLGregorianCalendar] {
    def writesXML(__obj: XMLGregorianCalendar, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toXMLFormat, Some(xstype), __namespace, __elementLabel, __scope)
  }
    
  implicit object __GregorianCalendarXMLWriter extends CanWriteXML[java.util.GregorianCalendar] {
    def writesXML(__obj: java.util.GregorianCalendar, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(Helper.toCalendar(__obj).toXMLFormat, None, __namespace, __elementLabel, __scope)
  }
    
  implicit object __QNameXMLFormat extends XMLFormat[javax.xml.namespace.QName] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, javax.xml.namespace.QName] = try {
      Right(javax.xml.namespace.QName.valueOf(seq.text))
    } catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: javax.xml.namespace.QName, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  }
  
  def qnameXMLWriter(xstype: String) = new CanWriteXML[javax.xml.namespace.QName] {
    def writesXML(__obj: javax.xml.namespace.QName, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some(xstype), __namespace, __elementLabel, __scope)
  }
  
  implicit object __StringArrayXMLFormat extends XMLFormat[Array[String]] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, Array[String]] = try { Right(Helper.splitBySpace(seq.text)) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: Array[String], __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.mkString(" "), None, __namespace, __elementLabel, __scope)
  }
  
  def stringArrayXMLWriter(xstype: String) = new CanWriteXML[Array[String]] {
    def writesXML(__obj: Array[String], __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.mkString(" "), Some(xstype), __namespace, __elementLabel, __scope)
  }
  
  implicit object __ByteArrayXMLFormat extends XMLFormat[Array[Byte]] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, Array[Byte]] = try { Right(Helper.toByteArray(seq.text)) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: Array[Byte], __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(Helper.toString(__obj), None, __namespace, __elementLabel, __scope)
  }
  
  def byteArrayXMLWriter(xstype: String) = new CanWriteXML[Array[Byte]] {
    def writesXML(__obj: Array[Byte], __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(Helper.toString(__obj), Some(xstype), __namespace, __elementLabel, __scope)
  }
  
  implicit object __HexBinaryXMLFormat extends XMLFormat[HexBinary] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, HexBinary] = try { Right(Helper.toHexBinary(seq.text)) }
      catch { case e: Exception => Left(e.toString) }
    
    def writesXML(__obj: HexBinary, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(Helper.toString(__obj), None, __namespace, __elementLabel, __scope)
  }
  
  object XSHexBinaryXMLWriter extends CanWriteXML[HexBinary] {
    def writesXML(__obj: HexBinary, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(Helper.toString(__obj), None, __namespace, __elementLabel, __scope)
  }
  
  implicit object __URIXMLFormat extends XMLFormat[java.net.URI] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, java.net.URI] = try { Right(Helper.toURI(seq.text)) }
      catch { case e: Exception => Left(e.toString) }
      
    def writesXML(__obj: java.net.URI, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some("hexBinary"), __namespace, __elementLabel, __scope)
  }
  
  object XSAnyURIXMLWriter extends CanWriteXML[java.net.URI] {
    def writesXML(__obj: java.net.URI, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML(__obj.toString, Some("anyURI"), __namespace, __elementLabel, __scope)
  }
  
  implicit def seqXMLFormat[A: XMLFormat]: XMLFormat[Seq[A]] = new XMLFormat[Seq[A]] {
    def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, Seq[A]] = try {
      val xs = Helper.splitBySpace(seq.text).toSeq
      Right(xs map { x => Scalaxb.fromXML[A](scala.xml.Text(x)) })
    } catch { case e: Exception => Left(e.toString) }
    
    def writesXML(__obj: Seq[A], __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.stringToXML((__obj map { x => Scalaxb.toXML(x, __namespace, __elementLabel, __scope, __typeAttribute).text }).mkString(" "),
        None, __namespace, __elementLabel, __scope)       
  }
  
  implicit def dataRecordXMLWriter[A]: CanWriteXML[DataRecord[A]] = new CanWriteXML[DataRecord[A]] {      
    def writesXML(__obj: DataRecord[A], __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      DataRecord.toXML(__obj, __namespace, __elementLabel, __scope, __typeAttribute)   
  }
  
  implicit object NoneXMLWriter extends CanWriteXML[None.type] {  
    def writesXML(__obj: None.type, __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Helper.nilElem(__namespace, __elementLabel.get, __scope)    
  }
  
  implicit def someXMLWriter[A: CanWriteXML]: CanWriteXML[Some[A]] = new CanWriteXML[Some[A]] {
    def writesXML(__obj: Some[A], __namespace: Option[String], __elementLabel: Option[String],
        __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
      Scalaxb.toXML[A](__obj.get, __namespace, __elementLabel, __scope, __typeAttribute)
  } 
  
  // implicit object AnyXMLWriter extends CanWriteXML[Any] {
  //   def writesXML(__obj: Any, __namespace: Option[String], __elementLabel: Option[String],
  //       __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
  //     Helper.stringToXML(__obj.toString, None, __namespace, __elementLabel, __scope)
  // } 
}

trait DataRecord[+A] {
  val namespace: Option[String]
  val key: Option[String]
  val value: A
  
  override def toString: String = {
    "DataRecord(" +
    ((namespace, key, value) match {
      case (None, Some(k), _) => k + "," + value.toString
      case (Some(n), Some(k), _) => "{" + n + "}" + k + "," + value.toString
      case _ => value.toString
    }) + ")"
  }
}

object DataRecord {
  private case class DataWriter[+A](
    namespace: Option[String],
    key: Option[String],
    xstypeNamespace: Option[String],
    xstypeName: Option[String],
    value: A,
    writer: CanWriteXML[_]) extends DataRecord[A]
  import Helper._
  
  // this is for nil element.
  def apply(namespace: Option[String], key: Option[String], value: None.type): DataRecord[Option[Nothing]] =
    DataWriter(namespace, key, None, None, value, XMLFormat.NoneXMLWriter)
  
  // this is for choice option: DataRecord(x.namespace, Some(x.name), fromXML[Address](x))
  def apply[A:CanWriteXML](namespace: Option[String], key: Option[String], value: A): DataRecord[A] =
    DataWriter(namespace, key, None, None, value, implicitly[CanWriteXML[A]])
  
  def apply[A:CanWriteXML](value: A): DataRecord[A] =
    apply(None, None, value)
  
  def apply[A:CanWriteXML](namespace: Option[String], key: Option[String],
      xstypeNamespace: Option[String], xstypeName: Option[String], value: A): DataRecord[A] =
    DataWriter(namespace, key, xstypeNamespace, xstypeName, value, implicitly[CanWriteXML[A]])
  
  // this is for any.
  def apply(elemName: ElemName): DataRecord[Any] = {
    import XMLFormat._
    
    val ns = elemName.namespace
    val key = Some(elemName.name)
    val XS = Some(XML_SCHEMA_URI)
    
    if (elemName.name == "") DataWriter(None, None, None, None, elemName.text, XMLFormat.__StringArrayXMLFormat)
    else instanceType(elemName.node) match {
      case (XS, xstype)   =>
        xstype match {
          case Some("int")                => DataRecord(ns, key, XS, xstype, elemName.text.toInt)
          case Some("byte")               => DataRecord(ns, key, XS, xstype, elemName.text.toByte)
          case Some("short")              => DataRecord(ns, key, XS, xstype, elemName.text.toShort)
          case Some("long")               => DataRecord(ns, key, XS, xstype, elemName.text.toLong)
          case Some("float")              => DataRecord(ns, key, XS, xstype, elemName.text.toFloat)
          case Some("double")             => DataRecord(ns, key, XS, xstype, elemName.text.toDouble)
          case Some("integer")            => DataRecord(ns, key, XS, xstype, elemName.text.toInt)
          case Some("nonPositiveInteger") => DataRecord(ns, key, XS, xstype, elemName.text.toInt)
          case Some("negativeInteger")    => DataRecord(ns, key, XS, xstype, elemName.text.toInt)
          case Some("nonNegativeInteger") => DataRecord(ns, key, XS, xstype, elemName.text.toInt)
          case Some("positiveInteger")    => DataRecord(ns, key, XS, xstype, elemName.text.toInt)
          case Some("unsignedLong")       => DataRecord(ns, key, XS, xstype, BigInt(elemName.text))
          case Some("unsignedInt")        => DataRecord(ns, key, XS, xstype, elemName.text.toLong)
          case Some("unsignedShort")      => DataRecord(ns, key, XS, xstype, elemName.text.toInt)
          case Some("unsignedByte")       => DataRecord(ns, key, XS, xstype, elemName.text.toInt)
          case Some("decimal")            => DataRecord(ns, key, XS, xstype, BigDecimal(elemName.text))
          case Some("boolean")            => DataRecord(ns, key, XS, xstype, elemName.text.toBoolean)
          case Some("string")             => DataRecord(ns, key, XS, xstype, elemName.text)
          case Some("normalizedString")   => DataRecord(ns, key, XS, xstype, elemName.text)
          case Some("token")              => DataRecord(ns, key, XS, xstype, elemName.text)
          case Some("language")           => DataRecord(ns, key, XS, xstype, elemName.text)
          case Some("Name")               => DataRecord(ns, key, XS, xstype, elemName.text)
          case Some("NCName")             => DataRecord(ns, key, XS, xstype, elemName.text)
          case Some("NMTOKEN")            => DataRecord(ns, key, XS, xstype, elemName.text)
          case Some("NMTOKENS")           => DataRecord(ns, key, XS, xstype, elemName.splitBySpace)
          case Some("ID")                 => DataRecord(ns, key, XS, xstype, elemName.text)
          case Some("IDREF")              => DataRecord(ns, key, XS, xstype, elemName.text)
          case Some("IDREFS")             => DataRecord(ns, key, XS, xstype, elemName.splitBySpace)
          case Some("ENTITY")             => DataRecord(ns, key, XS, xstype, elemName.text)
          case Some("ENTITIES")           => DataRecord(ns, key, XS, xstype, elemName.splitBySpace)
          case Some("hexBinary")          => DataRecord(ns, key, XS, xstype, toHexBinary(elemName.text))
          case Some("base64Binary")       => DataRecord(ns, key, XS, xstype, toByteArray(elemName.text))
          case Some("anyURI")             => DataRecord(ns, key, XS, xstype, new java.net.URI(elemName.text))
          case Some("QName")              => DataRecord(ns, key, XS, xstype, javax.xml.namespace.QName.valueOf(elemName.text))
          case Some("NOTATION")           => DataRecord(ns, key, XS, xstype, javax.xml.namespace.QName.valueOf(elemName.text))
          case Some("duration")           => DataRecord(ns, key, XS, xstype, Helper.toDuration(elemName.text))
          case Some("dateTime")           => DataRecord(ns, key, XS, xstype, XMLCalendar(elemName.text))
          case Some("time")               => DataRecord(ns, key, XS, xstype, XMLCalendar(elemName.text))
          case Some("gYearMonth")         => DataRecord(ns, key, XS, xstype, XMLCalendar(elemName.text))
          case Some("gYear")              => DataRecord(ns, key, XS, xstype, XMLCalendar(elemName.text))
          case Some("gMonthDay")          => DataRecord(ns, key, XS, xstype, XMLCalendar(elemName.text))
          case Some("gDay")               => DataRecord(ns, key, XS, xstype, XMLCalendar(elemName.text))
          case Some("gMonth")             => DataRecord(ns, key, XS, xstype, XMLCalendar(elemName.text))
          
          case _          => DataRecord(ns, key, XS, xstype, elemName.node)
        }      
      case _ => 
        val (xsns, xstype) = instanceType(elemName.node)
        DataRecord(ns, key, xsns, xstype, elemName.node)
    }
  }
  
  def unapply[A](record: DataRecord[A]): Option[(Option[String], Option[String], A)] =
    Some(record.namespace, record.key, record.value)
  
  def toXML[A](__obj: DataRecord[A], __namespace: Option[String], __elementLabel: Option[String],
      __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq = __obj match {
    case w: DataWriter[_] =>
      __obj.value match {
        case seq: NodeSeq =>
          w.writer.asInstanceOf[CanWriteXML[A]].writesXML(__obj.value, __namespace, __elementLabel, __scope, __typeAttribute)
        case _ =>
          w.writer.asInstanceOf[CanWriteXML[A]].writesXML(__obj.value, __namespace, __elementLabel, __scope, false) match {
            case elem: Elem if (w.xstypeNamespace.isDefined && w.xstypeName.isDefined &&
                __scope.getPrefix(w.xstypeNamespace.get) != null &&
                __scope.getPrefix(XSI_URL) != null) =>
              elem % new PrefixedAttribute(__scope.getPrefix(XSI_URL), "type",
                __scope.getPrefix(w.xstypeNamespace.get) + ":" + w.xstypeName.get, scala.xml.Null)
            case x => x
          }
      }
    case _ => error("unknown DataRecord.")
  }
}

case class ElemName(namespace: Option[String], name: String) {
  var node: scala.xml.Node = _
  def text = node.text
  def nil = Helper.isNil(node)
  def nilOption: Option[ElemName] = if (nil) None else Some(this)
  def splitBySpace = Helper.splitBySpace(text)
}

trait AnyElemNameParser extends scala.util.parsing.combinator.Parsers {
  import XMLFormat._
  
  type Elem = ElemName
  
  // we need this so treat ElemName as NodeSeq for fromXML etc.
  implicit def toNodeSeq(elem: Elem): scala.xml.NodeSeq = elem.node
  
  def any: Parser[ElemName] = 
    accept("any", { case x: ElemName if x.name != "" => x })  
  
  def optTextRecord = opt(text ^^ (x => DataRecord(x.node.text)))
  
  def text: Parser[ElemName] =
    accept("text", { case x: ElemName if x.name == "" => x })
}

trait CanWriteChildNodes[A] extends CanWriteXML[A] {
  def typeName: Option[String] = None
  def writesAttribute(__obj: A, __scope: scala.xml.NamespaceBinding): scala.xml.MetaData = scala.xml.Null
  def writesChildNodes(__obj: A, __scope: scala.xml.NamespaceBinding): Seq[scala.xml.Node]
  
  def writesXML(__obj: A, __namespace: Option[String], __elementLabel: Option[String], 
      __scope: scala.xml.NamespaceBinding, __typeAttribute: Boolean): scala.xml.NodeSeq =
    scala.xml.Elem(Helper.getPrefix(__namespace, __scope).orNull,
      __elementLabel getOrElse { error("missing element label.") },
      if (__typeAttribute && typeName.isDefined &&
          __scope.getPrefix(Helper.XSI_URL) != null) scala.xml.Attribute(__scope.getPrefix(Helper.XSI_URL), "type", typeName.get,
        writesAttribute(__obj, __scope))
      else writesAttribute(__obj, __scope),
      __scope,
      writesChildNodes(__obj, __scope): _*)  
}

trait ElemNameParser[A] extends AnyElemNameParser with XMLFormat[A] with CanWriteChildNodes[A] {
  def readsXMLEither(seq: scala.xml.NodeSeq): Either[String, A] = seq match {
    case node: scala.xml.Node =>
      parse(parser(node), node.child) match {
        case x: Success[_] => Right(x.get)
        case x: Failure => Left(x.msg)
        case x: Error => Left(x.msg)
      }
    case _ => Left("seq must be scala.xml.Node")
  }
  
  def parser(node: scala.xml.Node): Parser[A]
  def isMixed: Boolean = false
  
  def parse[A](p: Parser[A], in: Seq[scala.xml.Node]): ParseResult[A] = 
    p(new ElemNameSeqReader(elementNames(in)))
  
  def elementNames(in: Seq[scala.xml.Node]): Seq[ElemName] =
    if (isMixed) in map { toElemName }
    else in collect { case x: scala.xml.Elem => toElemName(x) }
    
  def toElemName(in: scala.xml.Node) = in match {
    case x: scala.xml.Elem =>    
      val elemName = ElemName(Option[String](x.scope.getURI(x.prefix)), x.label)
      elemName.node = x
      elemName
    case _ =>
      val elemName = ElemName(None, "")
      elemName.node = in
      elemName      
  }
}

class ElemNameSeqReader(seq: Seq[ElemName],
    override val offset: Int) extends scala.util.parsing.input.Reader[ElemName] {
  import scala.util.parsing.input._
  
  def this(seq: Seq[ElemName]) = this(seq, 0)
  
  override def first: ElemName  =
    if (seq.isDefinedAt(offset)) seq(offset)
    else null
    
  def rest: ElemNameSeqReader =
    if (seq.isDefinedAt(offset)) new ElemNameSeqReader(seq, offset + 1)
    else this
  
  def pos: Position = new ElemNameSeqPosition(seq, offset)
  
  def atEnd = !seq.isDefinedAt(offset)
  
  override def drop(n: Int): ElemNameSeqReader = 
    new ElemNameSeqReader(seq, offset + n)
}

class ElemNameSeqPosition(val source: Seq[ElemName], val offset: Int) extends 
    scala.util.parsing.input.Position {
  protected def lineContents =
    source.toString
    
  override def line = 1
  override def column = offset + 1
}

class HexBinary(_length: Int) extends scala.collection.IndexedSeq[Byte] {
  private var array = new Array[Byte](_length)
  
  def length = array.length
  def apply(idx: Int): Byte = array(idx)
  override def toString: String = Helper.toString(this)
}

object HexBinary {
  def apply(xs: Byte*) = {
    val value = new HexBinary(xs.length)
    var i = 0
    for (x <- xs.iterator) { value.array(i) = x; i += 1 }
    value
  }
  
  def unapplySeq[Byte](x: HexBinary) = Some(x.toIndexedSeq)
}

object XMLCalendar {
  def apply(value: String): XMLGregorianCalendar = Helper.toCalendar(value)
  def unapply(value: XMLGregorianCalendar): Option[String] = Some(value.toXMLFormat)
}

object Helper {
  val XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema"
  val XSI_URL = "http://www.w3.org/2001/XMLSchema-instance"
  val XSI_PREFIX = "xsi"

  def toCalendar(value: String): XMLGregorianCalendar = {
    import javax.xml.datatype._
    val typeFactory = javax.xml.datatype.DatatypeFactory.newInstance()
    typeFactory.newXMLGregorianCalendar(value)
  }
  
  def toCalendar(value: java.util.GregorianCalendar): XMLGregorianCalendar = {
    import javax.xml.datatype._
    import java.util.{GregorianCalendar, Calendar => JCalendar}
    
    val typeFactory = javax.xml.datatype.DatatypeFactory.newInstance()
    val xmlGregorian = typeFactory.newXMLGregorianCalendar()
    if (value.getTimeZone != null) {
      xmlGregorian.setTimezone(value.getTimeZone.getRawOffset / 60000)
    }
        
    if (value.isSet(JCalendar.YEAR)) xmlGregorian.setYear(if (value.get(JCalendar.ERA) == GregorianCalendar.AD) value.get(JCalendar.YEAR) else -value.get(JCalendar.YEAR))
    if (value.isSet(JCalendar.MONTH)) xmlGregorian.setMonth(value.get(JCalendar.MONTH) - JCalendar.JANUARY + DatatypeConstants.JANUARY)
    if (value.isSet(JCalendar.DAY_OF_MONTH)) xmlGregorian.setDay(value.get(JCalendar.DAY_OF_MONTH))
    if (value.isSet(JCalendar.HOUR_OF_DAY)) xmlGregorian.setHour(value.get(JCalendar.HOUR_OF_DAY))
    if (value.isSet(JCalendar.MINUTE)) xmlGregorian.setMinute(value.get(JCalendar.MINUTE))
    if (value.isSet(JCalendar.SECOND)) xmlGregorian.setSecond(value.get(JCalendar.SECOND))
    if (value.isSet(JCalendar.MILLISECOND) && value.get(JCalendar.MILLISECOND) > 0) xmlGregorian.setFractionalSecond(new java.math.BigDecimal(value.get(JCalendar.MILLISECOND)))
    
    xmlGregorian
  }
  
  def toString(value: Array[Byte]): String =
    (new sun.misc.BASE64Encoder()).encodeBuffer(value).stripLineEnd
  
  def toString(value: HexBinary): String =
    (value map { x => ("0" + Integer.toHexString(x.toInt)).takeRight(2) }).mkString.toUpperCase
  
  def toDuration(value: String) = {
    val typeFactory = javax.xml.datatype.DatatypeFactory.newInstance()
    typeFactory.newDuration(value)
  }
  
  def toByteArray(value: String) =
    (new sun.misc.BASE64Decoder()).decodeBuffer(value)
  
  def toHexBinary(value: String) = {
    val array = new Array[Byte](value.length / 2)
    for (i <- 0 to array.length - 1) {
      array(i) = Integer.parseInt(value.drop(i * 2).take(2), 16).toByte
    }
    HexBinary(array: _*)
  }
  
  def toURI(value: String) =
    java.net.URI.create(value)
    
  def isNil(node: scala.xml.Node) =
    (node \ ("@{" + XSI_URL + "}nil")).headOption map { _.text == "true" } getOrElse {
      false
    }
  
  def nilElem(namespace: Option[String], elementLabel: String,
      scope: scala.xml.NamespaceBinding) =
    scala.xml.Elem(getPrefix(namespace, scope).orNull, elementLabel,
      scala.xml.Attribute(scope.getPrefix(XSI_URL), "nil", "true", scala.xml.Null),
      scope, Nil: _*)
      
  def splitBySpace(text: String) = text.split(' ').filter("" !=)
  
  def instanceType(node: scala.xml.Node): (Option[String], Option[String]) = {
    val typeName = (node \ ("@{" + XSI_URL + "}type")).text
    val prefix = if (typeName.contains(':')) Some(typeName.dropRight(typeName.length - typeName.indexOf(':')))
      else None
    val namespace = Option[String](node.scope.getURI(prefix.orNull))
    val value = if (typeName.contains(':')) typeName.drop(typeName.indexOf(':') + 1)
      else typeName
    (namespace, if (value == "") None else Some(value))
  }
  
  def getPrefix(namespace: Option[String], scope: scala.xml.NamespaceBinding) =
    if (Option[String](scope.getURI(null)) == namespace) None
    else Option[String](scope.getPrefix(namespace.orNull))
  
    
  def writesXML(__obj: String, __xstype: Option[String], __namespace: Option[String], __elementLabel: Option[String],
       __scope: scala.xml.NamespaceBinding): scala.xml.NodeSeq = {
    __elementLabel map { label =>
      scala.xml.Elem(getPrefix(__namespace, __scope).orNull, label,
        __xstype map { x => 
          new scala.xml.PrefixedAttribute(__scope.getPrefix(XSI_URL), "type", x, scala.xml.Null)
        } getOrElse { scala.xml.Null },
        __scope, scala.xml.Text(__obj.toString))
    } getOrElse { scala.xml.Text(__obj) }
  }
  
  def stringToXML(__obj: String, __xstype: Option[String], __namespace: Option[String], __elementLabel: Option[String],
       __scope: scala.xml.NamespaceBinding): scala.xml.NodeSeq = {
    __elementLabel map { label =>
      scala.xml.Elem(getPrefix(__namespace, __scope).orNull, label,
        __xstype map { x => 
          new scala.xml.PrefixedAttribute(__scope.getPrefix(XSI_URL), "type", x, scala.xml.Null)
        } getOrElse { scala.xml.Null },
        __scope, scala.xml.Text(__obj.toString))
    } getOrElse { scala.xml.Text(__obj) }
  }
}
