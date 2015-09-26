package tmt.app

import org.widok.Route
import tmt.pages.{NotFound, MainPage, TestPage}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

object Routes {
  val main = Route("/", MainPage.apply)
  val test = Route("/test/:param", TestPage.apply)
  val notFound = Route("/404", NotFound.apply)

  val routes = Set(main, test, notFound)
}
