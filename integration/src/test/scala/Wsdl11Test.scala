import scalaxb.compiler.{Verbose}
import scalaxb.compiler.wsdl11.{Driver}
import java.io.{File}
import scalaxb.compiler.{Config}

object Wsdl11Test extends TestBase {
  override val module = new Driver // with Verbose

  lazy val generated = module.process(inFile,
    Config(packageNames = Map(None -> Some(packageName)),
      packageDir = true, outdir = tmp))
  val dependencyPath = new File("integration/lib_managed/scala_2.9.0/test/").getAbsoluteFile.listFiles.toList

  val packageName = "genericbarcode"
  val inFile  = new File("integration/src/test/resources/genericbarcode.wsdl")
  "stockquote.scala file must compile" in {
    (List("""import genericbarcode._""",
       """val service = (new BarCodeSoap12s with scalaxb.SoapClients with scalaxb.DispatchHttpClients {}).service
       val data = BarCodeData(120, 120, 0, 1, 1, 20, 20, true, None, None, None, 10.0f, Both, CodeEAN128B, NoneType, BottomCenter, PNG)
       println(scalaxb.toXML(data, "BarCodeParam", defaultScope))
       val response = service.generateBarCode(data, Some("1234")).right.get.get
       println(response)""",
       """response.toString.contains("iVB")"""), generated) must evaluateTo(true,
      outdir = "./tmp", classpath = dependencyPath map {_.toString})
  }

//  val packageName = "stockquote"
//  val inFile  = new File("integration/src/test/resources/stockquote.wsdl")
//  "stockquote.scala file must compile" in {
//    (List("""val service = (new stockquote.StockQuoteSoap12s with scalaxb.SoapClients with scalaxb.DispatchHttpClients {}).service
//       val response = service.getQuote(Some("GOOG"))""",
//       """response.toString.contains("<Symbol>GOOG</Symbol>")"""), generated) must evaluateTo(true,
//      outdir = "./tmp", classpath = dependencyPath map {_.toString})
//  }
}
