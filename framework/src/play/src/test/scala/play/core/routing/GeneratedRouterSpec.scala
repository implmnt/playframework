/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package play.core.routing

import org.specs2.mutable.Specification
import play.api.http.{ DefaultHttpErrorHandler, HttpErrorHandler }
import play.api.mvc._
import play.api.routing.{ HandlerDef, Router }
import play.core.j.{ JavaAction, JavaHandler }
import play.core.test.FakeRequest

object GeneratedRouterSpec extends Specification {

  class TestRouter[H](
      handlerThunk: => H,
      handlerDef: HandlerDef,
      override val errorHandler: HttpErrorHandler = DefaultHttpErrorHandler,
      val prefix: String = "/")(implicit hif: HandlerInvokerFactory[H]) extends GeneratedRouter {

    override def withPrefix(prefix: String): Router = new TestRouter[H](handlerThunk, handlerDef, errorHandler, prefix)

    // The following code is based on the code generated by the routes compiler.

    private[this] lazy val route = Route("GET", PathPattern(List(StaticPart(this.prefix))))
    private[this] lazy val invoker = createInvoker(
      handlerThunk,
      handlerDef
    )
    override def routes: PartialFunction[RequestHeader, Handler] = {
      case route(params) => call { invoker.call(handlerThunk) }
    }
    override def documentation: Seq[(String, String, String)] = List(
      ("GET", this.prefix, "TestRouter.handler")
    )
  }

  class JavaController extends play.mvc.Controller {
    def index = play.mvc.Results.ok("Hello world")
  }

  def routeToHandler[H, A](handlerThunk: => H, handlerDef: HandlerDef, request: RequestHeader)(block: Handler => A)(implicit hif: HandlerInvokerFactory[H]): A = {
    val router = new TestRouter(handlerThunk, handlerDef)
    val request = FakeRequest()
    val routedHandler = router.routes(request)
    block(routedHandler)
  }

  "A GeneratedRouter" should {

    "route requests to Scala controllers" in {
      val Action = ActionBuilder.ignoringBody
      val handler = Action(Results.Ok("Hello world"))
      val handlerDef = HandlerDef(
        handler.getClass.getClassLoader,
        "router",
        "ControllerClassName",
        "handler",
        Nil,
        "GET",
        "Comment",
        "/"
      )
      val request = FakeRequest()
      routeToHandler(handler, handlerDef, request) { routedHandler: Handler =>
        routedHandler must haveInterface[Handler.Stage]
        val (preprocessedRequest, preprocessedHandler) = Handler.applyStages(request, routedHandler)
        preprocessedHandler must_== handler
        preprocessedRequest.path must_== "/"
        preprocessedRequest.tags(play.api.routing.Router.Tags.RoutePattern) must_== "/"
        preprocessedRequest.tags(play.api.routing.Router.Tags.RouteVerb) must_== "GET"
        preprocessedRequest.tags(play.api.routing.Router.Tags.RouteController) must_== "ControllerClassName"
        preprocessedRequest.tags(play.api.routing.Router.Tags.RouteActionMethod) must_== "handler"
        preprocessedRequest.tags(play.api.routing.Router.Tags.RouteComments) must_== "Comment"
        preprocessedRequest.attrs(play.api.routing.Router.Attrs.HandlerDef) must_== handlerDef
      }
    }

    "route requests to Java controllers" in {
      val controller = new JavaController
      val handlerDef = HandlerDef(
        controller.getClass.getClassLoader,
        "router",
        controller.getClass.getName,
        "index",
        Nil,
        "GET",
        "Comment",
        "/"
      )
      val request = FakeRequest()
      routeToHandler(controller.index, handlerDef, request) { routedHandler: Handler =>
        routedHandler must haveInterface[Handler.Stage]
        val (preprocessedRequest, preprocessedHandler) = Handler.applyStages(request, routedHandler)
        preprocessedHandler must haveInterface[JavaHandler]
        preprocessedRequest.path must_== "/"
        preprocessedRequest.tags(play.api.routing.Router.Tags.RoutePattern) must_== "/"
        preprocessedRequest.tags(play.api.routing.Router.Tags.RouteVerb) must_== "GET"
        preprocessedRequest.tags(play.api.routing.Router.Tags.RouteController) must_== controller.getClass.getName
        preprocessedRequest.tags(play.api.routing.Router.Tags.RouteActionMethod) must_== "index"
        preprocessedRequest.tags(play.api.routing.Router.Tags.RouteComments) must_== "Comment"
        preprocessedRequest.attrs(play.api.routing.Router.Attrs.HandlerDef) must_== handlerDef
      }
    }

  }

}
