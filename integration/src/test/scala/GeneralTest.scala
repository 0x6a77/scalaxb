import java.io.{File}
import scalaxb.compiler._
import scalaxb.compiler.xsd.Driver

object GeneralTest extends TestBase {
  // override val module: Module = new Driver with Verbose
  val inFile    = new File("integration/src/test/resources/general.xsd")
  val usageFile = new File(tmp, "GeneralUsage.scala")
  val custumFile = new File(tmp, "CustomizationUsage.scala")
  
  lazy val generated = module.process(inFile, "general", tmp)
  copyFileFromResource("GeneralUsage.scala", usageFile)
  copyFileFromResource("CustomizationUsage.scala", custumFile)
  
  "general.scala file must compile together with GeneralUsage.scala" in {
    (List("GeneralUsage.allTests"),
      usageFile :: generated) must evaluateTo(true,
      outdir = "./tmp")
  }
  
  "general.scala file must compile together with CustomizationUsage.scala" in {
    (List("CustomizationUsage.allTests"),
      custumFile :: generated) must evaluateTo(true,
      outdir = "./tmp")
  }
}
