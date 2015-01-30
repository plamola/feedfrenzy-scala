package nl.dekkr.feedfrenzy.streams.flows

import java.net.SocketTimeoutException

import akka.stream.stage.{ Context, Directive, PushStage }
import nl.dekkr.feedfrenzy.model.ContentBlock

import scala.util.{ Failure, Success, Try }
import scalaj.http.Http

/**
 * Author: matthijs
 * Created on: 20 Jan 2015.
 */
class GetPageContent extends PushStage[ContentBlock, ContentBlock] {

  override def onPush(elem: ContentBlock, ctx: Context[ContentBlock]): Directive =
    try {
      // TODO replace hard-coded charset with value from scraper
      elem.sourceUrl match {
        case None => ctx.push(elem)
        case Some(uri) =>
          Try(pageContent(elem.sourceUrl.get).charset("UTF-8")) match {
            case Success(content) =>
              ctx.push(ContentBlock(scraperDefinition = elem.scraperDefinition, pageContent = Some(content.asString), sourceUrl = elem.sourceUrl, result = elem.result))
            case Failure(e) =>
              // TODO log error
              println(s"Error getting content for url: [${elem.sourceUrl.get}] [${elem.scraperDefinition.scraper.id.get}] [${e.getMessage}]")
              ctx.pull()
          }
      }
    } catch {
      case timeout: SocketTimeoutException =>
        // TODO log error
        println(s"Time out: [${elem.sourceUrl.get}] [${elem.scraperDefinition.scraper.id.get}]")
        ctx.pull()
      case e: Exception =>
        // TODO log error
        println(s"Exception: [${elem.sourceUrl.get}] [${elem.scraperDefinition.scraper.id.get}] [${e.getMessage}] [${e.getCause}] ")
        ctx.pull()

    }

  private val USER_AGENT: String = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.99 Safari/537.36"

  def pageContent(uri: String) = Http(uri).header("User-Agent", USER_AGENT)

}