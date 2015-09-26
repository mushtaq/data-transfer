package tmt.pages

import org.widok._

import scala.concurrent.{ExecutionContext, Future}

case class TestPage(implicit ec: ExecutionContext) extends Page {

  def render(route: InstantiatedRoute) = Future {
    Inline(
      "received params: ",
      route.args.toString()
    )
  }

  override def ready(node: Node) = {
    log(s"read with node: $node")
  }
}
