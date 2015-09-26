package tmt.pages

import org.scalajs.dom
import org.widok._
import org.widok.bindings.HTML
import tmt.app.Routes
import scala.concurrent.{ExecutionContext, Future}

case class NotFound(implicit ec: ExecutionContext) extends Page {
  def render(route: InstantiatedRoute) = Future {
    HTML.Heading.Level1("Page not found")
  }

  override def ready(node: Node) = {
    log(s"ready with node: $node")
    dom.setTimeout(() => Routes.main().go(), 2000)
  }
}
