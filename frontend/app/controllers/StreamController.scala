package controllers

import javax.inject.{Inject, Singleton}

import common.AppSettings
import library.GenericExtensions.RichGeneric
import models.{RoleMappingsFactory, HostMappings}
import play.api.mvc.{Action, Controller}
import services.ClusterClientService

import scala.concurrent.duration.DurationLong

@Singleton
class StreamController @Inject()(
  appSettings: AppSettings,
  clusterClientService: ClusterClientService,
  roleMappingsFactory: RoleMappingsFactory
) extends Controller {

  private val roleMappings = roleMappingsFactory.fromConfig(appSettings.bindings)
  private val hostMappings = HostMappings(appSettings.hosts)

  def streams() = Action {
    Ok(views.html.streams(hostMappings, roleMappings))
  }

  def throttle(serverName: String, delay: Long) = Action {
    clusterClientService.throttle(serverName, delay.millis)
    Accepted("ok")
  }

  def subscribe(serverName: String, topic: String) = Action {
    clusterClientService.subscribe(serverName, topic)
    Accepted("ok")
  }

  def unsubscribe(serverName: String, topic: String) = Action {
    clusterClientService.unsubscribe(serverName, topic)
    Accepted("ok")
  }

  def servers(role: String) = Action {
    Ok(roleMappings.getServers(role).toJson)
  }

}
