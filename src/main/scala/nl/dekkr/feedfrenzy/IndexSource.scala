package nl.dekkr.feedfrenzy

import akka.actor.{ ActorSystem, Props }
import akka.stream.FlowMaterializer
import akka.stream.actor.{ ActorSubscriber, ActorPublisher }
import akka.stream.scaladsl._
import nl.dekkr.feedfrenzy.model.{ ContentBlock, IndexPage, Scraper }
import nl.dekkr.feedfrenzy.streams._
import nl.dekkr.feedfrenzy.streams.flows.SplitIndexIntoBlocks
import nl.dekkr.feedfrenzy.streams.sinks.IndexPageSubscriber
import nl.dekkr.feedfrenzy.streams.sources.ScraperActorPublisher
import org.reactivestreams.{ Publisher, Subscriber }
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

    val publisher: Publisher[Scraper] = ActorPublisher[Scraper](system.actorOf(Props[ScraperActorPublisher], "JobSource"))
    val subscriber: Subscriber[IndexPage] = ActorSubscriber[IndexPage](system.actorOf(Props[IndexPageSubscriber], "IndexPageSubscriber"))

    val indexPageFlow: Flow[Scraper, IndexPage] = Flow[Scraper]
      .map(
        thisScraper => {
          val content = pageContent(thisScraper.sourceUrl)
          println(s"IndexPageFlow: [${thisScraper.id.getOrElse(0)}] - url: ${thisScraper.sourceUrl} (blocks: ${content.split("<div").length})")
          IndexPage(scraper = thisScraper, content = Some(content))
        }
      )

    val splitIntoBlocks: Flow[IndexPage, ContentBlock] = Flow[IndexPage]
      .transform(() => new SplitIndexIntoBlocks())

    val contentSink = ForeachSink[IndexPage] {
      el =>
        println(s"ContentSink: [${el.scraper.id.getOrElse(0)}] -  Content length: ${el.content.getOrElse("").length}")
        Thread.sleep(1000)
    }

    val loggerSink = ForeachSink[IndexPage] {
      el =>
        println(s"LoggerSink: [${el.scraper.id.getOrElse(0)}]")
    }

    val printSink = ForeachSink[ContentBlock] {
      value =>
        println(s"PrintSink: [${value.content}]")
    }

    val materialized = FlowGraph {
      implicit b =>
        import FlowGraphImplicits._
        val broadcast = Broadcast[IndexPage]
        Source(publisher) ~> indexPageFlow ~> broadcast ~> Sink(subscriber)
        broadcast ~> splitIntoBlocks ~> printSink
      //       broadcast ~> contentSink
      //        broadcast ~> loggerSink
    }

    materialized.run

    // import system.dispatcher
    //    materialized.get(loggerSink).onComplete {
    //      case Success(_) =>
    //        //Try(output.close())
    //        system.shutdown()
    //      case Failure(e) =>
    //        println(s"Failure: ${e.getMessage}")
    //        //Try(output.close())
    //        system.shutdown()
    //    }

  }

  def pageContent(uri: String): String = {
    Try(Http(uri).asString) match {
      case Success(content) => content
      case Failure(e)       => e.getMessage
    }
  }

}

