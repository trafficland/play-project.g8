import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import com.google.inject.{Guice, Injector}

object Global
  extends GlobalSettings {
  var injector: Injector = _    

  override def beforeStart(app: Application) {
    super.beforeStart(app)

    injector = Guice.createInjector(ApplicationModule)
  }

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    injector.getInstance(controllerClass)
  }

  override def onStart(app: Application) {
    Logger info "Application has started"
  }

  override def onStop(app: Application) {
    Logger info "Application has stopped"
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Logger error (ex.getMessage, ex)
    InternalServerError
  }
}