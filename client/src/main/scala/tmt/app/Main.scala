package tmt.app

import org.widok.RoutingApplication

object Main extends RoutingApplication(
  Routes.routes,
  Routes.notFound
)
