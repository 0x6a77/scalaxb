import org.specs._
import java.io.{File}

trait TestBase extends SpecificationWithJUnit with CompilerMatcher {
  val module = new org.scalaxb.compiler.xsd.Driver
  val tmp = new File("tmp")
  if (tmp.exists)
    deleteAll(tmp)
  tmp.mkdir
}
