package play.libs.ws

import play.libs.WS.HttpResponse

object FunctionalWebResponse {
    implicit def httpResponse2FunctionalWebReponse(response: HttpResponse) = FunctionalWebResponse(response)
}

object Status {
    def unapply(fwr: FunctionalWebResponse): Option[(Int, Int, Int)] = {
        val status = fwr.response.getStatus().intValue;
        Some((status / 100, status % 100 / 10, status % 10))
    }
}

case class UndesiredStatus(statusCode: Int, body: String) {
    override def toString() = "Undesired http status code " + statusCode + ":" + body
}

case class ResponseBody(private val response: HttpResponse) {
    def getXml() = response.getXml()
    def getJson() = response.getJson()
    def getString() = response.getString()
}

case class FunctionalWebResponse(response: HttpResponse) {

    def body = ResponseBody(response)

    def focusOnOK(): Either[UndesiredStatus, ResponseBody] = focusOn{case r@Status(2, 0, 0) => r.body}

    def focusOnStatus(statusCode: Int): Either[UndesiredStatus, ResponseBody] = {
        if (response.getStatus() == statusCode) {
            Right(ResponseBody(response))
        } else {
            Left(UndesiredStatus(response.getStatus().intValue, response.getString()))
        }
    }

    def focusOn[A](selectiveTransformer: PartialFunction[FunctionalWebResponse, A]): Either[UndesiredStatus, A] = {
        if (selectiveTransformer.isDefinedAt(this)) {
            Right(selectiveTransformer(this))
        } else {
            Left(UndesiredStatus(response.getStatus().intValue, response.getString()))
        }
    }

}
