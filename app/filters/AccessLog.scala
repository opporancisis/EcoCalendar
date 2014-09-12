package filters

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Filter
import play.api.mvc.RequestHeader
import play.api.mvc.Result

class AccessLog extends Filter {
  override def apply(next: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    val startTime = System.currentTimeMillis
    next(request).map { result =>
      val requestTime = System.currentTimeMillis - startTime
      val msg = s"${result.header.status}\t${requestTime}\t${request.method}\t${request.uri}\t${request.remoteAddress}\t" +
        s"${request.rawQueryString}\t" +
        s"${request.headers.get("referer").getOrElse("N/A")}\t" +
        s"[${request.headers.get("user-agent").getOrElse("N/A")}]"
      play.Logger.of("accesslog").info(msg)
      result //.withHeaders("Request-Time" -> requestTime.toString)
    }
  }
}