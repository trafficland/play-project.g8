package controllers

import play.api.mvc._
import play.api
import AppInfo._

object AdminController extends Controller {
  def getVersion = Action {
    Ok("%s : %s : %s".format(name, version, vendor))
  }
}