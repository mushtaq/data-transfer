package tmt.pages

import org.widok._
import org.widok.bindings.HTML
import tmt.app.Routes

import scala.concurrent.{ExecutionContext, Future}

case class MainPage(implicit ec: ExecutionContext) extends Page {

  def render(route: InstantiatedRoute) = Future {
    log(s"rendering with route: $route")
    HTML
      .Anchor("Link to second page")
      .url(Routes.test("param", "value"))
  }

  override def ready(node: Node) = {
    log(s"read with node: $node")
  }

  override def destroy() = {
    log("page main left")
  }
}
