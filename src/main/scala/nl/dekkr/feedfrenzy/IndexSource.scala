package nl.dekkr.feedfrenzy

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{ FlowGraph, Flow, Source, Sink }
import nl.dekkr.feedfrenzy.db.{ Tables, Schema }
import nl.dekkr.feedfrenzy.model.{ IndexPage, Scraper, Feed }

import scala.util.{ Try, Failure, Success }
import scalaj.http.Http
import scala.slick.driver.PostgresDriver.simple._

/**
 * Author: Matthijs
 * Created on: 11 Jan 2015.
 */

object IndexSource {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("feedfrenzy-collector")
    implicit val materializer = FlowMaterializer()

    val sourceList = getUpdatableFeeds

    // List all available feeds
    println("id \tupdated \t \t \turl")
    for { c <- sourceList } println("" + c.id.get + " \t" + c.updateddate + " \t" + c.feedurl)

    println("####################")
    println("Show stream results:")

    val scrapers = sourceList.map(feed => Scraper(id = feed.id, sourceUrl = feed.feedurl, singlePage = true))
    val src = Source(scrapers)

    val indexPageFlow: Flow[Scraper, String] = Flow[Scraper]
      .map(el => {
        println(s"Flow indexPageFlow url: ${el.sourceUrl}")
        pageContent(el.sourceUrl)
      }
      )

    val contentSink: Sink[String] = Sink.foreach[String](el => println(s"ContentSink - Content length: ${el.length}"))

    FlowGraph {
      implicit b =>
        import akka.stream.scaladsl.FlowGraphImplicits._
        src ~> indexPageFlow ~> contentSink
    }.run

  }

  def pageContent(uri: String): String = {
    Try(Http(uri).asString) match {
      case Success(content) => content
      case Failure(e)       => e.getMessage
    }

  }

  def getUpdatableFeeds: List[Feed] = {

    try {
      val feeds = TableQuery[Tables.FeedTable]
      implicit val session = Schema.getSession
      Schema.createOrUpdate(session)

      // Add some dummy feeds
      if (feeds.list.size < 1)
        feeds += Feed(feedurl = "http://nu.nl")

      if (feeds.list.size < 2)
        feeds += Feed(feedurl = "http://dagartikel.nl")

      feeds.list

    } catch {
      case e: Exception =>
        println(s"ERROR: ${e.getMessage} [${e.getCause}}]")
        List.empty
    }
  }
}