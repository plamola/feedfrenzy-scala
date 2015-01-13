package nl.dekkr.feedfrenzy

import akka.actor.{ ActorSystem, Props }
import akka.stream.FlowMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{ Flow, FlowGraph, Sink, Source }
import nl.dekkr.feedfrenzy.model.{ IndexPage, Scraper }
import nl.dekkr.feedfrenzy.streams.JobSourceActor

import scala.util.{ Failure, Success, Try }
import scalaj.http.Http

/**
 * Author: Matthijs
 * Created on: 11 Jan 2015.
 */

object IndexSource {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("feedfrenzy-collector")
    implicit val materializer = FlowMaterializer()

    println("####################")
    println("Show stream results:")

    val jobSourceActor = system.actorOf(Props[JobSourceActor])
    val src: Source[Scraper] = Source(ActorPublisher[Scraper](jobSourceActor))

    val indexPageFlow: Flow[Scraper, IndexPage] = Flow[Scraper]
      .map(el => {
        println(s"Flow indexPageFlow Scraper [${el.id.getOrElse(0)}] - url: ${el.sourceUrl}")
        IndexPage(scraper = el, content = Some(pageContent(el.sourceUrl)))
      }
      )

    //    val splitPageFlow : Flow[IndexPage, IndexPage] = Flow[IndexPage]
    //    .map( el =>
    //      // Make this dynamic, based on xpath / css selector
    //      // Extract UIDs
    //      el.content.getOrElse("").split("<div")
    //
    //      )

    val contentSink: Sink[IndexPage] =
      Sink.foreach[IndexPage](
        el =>
          println(s"Sink contentSink - Scraper [${el.scraper.id.getOrElse(0)}] -  Content length: ${el.content.getOrElse("").length}")
      )

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

}