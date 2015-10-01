package tmt.server

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import tmt.app.CustomDirectives
import tmt.io.{ImageReadService, ImageWriteService, MovieReadService, MovieWriteService}
import tmt.marshalling.BinaryMarshallers
import tmt.shared.models.Image

class MediaRoute(
  imageReadService: ImageReadService,
  movieReadService: MovieReadService,
  imageWriteService: ImageWriteService,
  movieWriteService: MovieWriteService
) extends BinaryMarshallers with CustomDirectives {

  val route = movieRoute ~ imageRoute

  lazy val movieRoute: Route = {
    path("movies" / "list") {
      get {
        complete(movieReadService.listMovies)
      }
    } ~
      pathPrefix("movies") {
        path(Rest) { name =>
          get {
            complete(movieReadService.sendMovie(name))
          } ~
            post {
              entity(as[Source[ByteString, Any]]) { byteStrings =>
                onSuccess(movieWriteService.copyMovie(name, byteStrings)) {
                  complete("copied")
                }
              }
            }
        }
      }
  }

  lazy val imageRoute: Route = {
    path("images" / "bytes") {
      get {
        handleWebsocketMessages(Sink.ignore, imageReadService.sendMessages) ~
        complete(imageReadService.sendBytes)
      } ~
      post {
        entity(as[Source[ByteString, Any]]) { byteStrings =>
          onSuccess(imageWriteService.copyBytes(byteStrings)) {
            complete("copied")
          }
        }
      }
    } ~
    path("images" / "objects") {
      get {
        complete(imageReadService.sendImages)
      } ~
      post {
        entity(as[Source[Image, Any]]) { images =>
          onSuccess(imageWriteService.copyImages(images)) {
            complete("copied")
          }
        }
      }
    }
  }
}
