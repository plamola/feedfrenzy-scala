package nl.dekkr.feedfrenzy

import akka.actor.{ ActorSystem, Props }
import akka.stream.FlowMaterializer
import akka.stream.actor.{ ActorSubscriber, ActorPublisher }
import akka.stream.scaladsl._
import nl.dekkr.feedfrenzy.model.{ IndexPage, Scraper }
import nl.dekkr.feedfrenzy.streams.{ IndexPageSubscriber, ScraperActorPublisher }
import org.reactivestreams.Subscriber
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

    val jobSourceActor = system.actorOf(Props[ScraperActorPublisher], "JobSource")
    val src: Source[Scraper] = Source(ActorPublisher[Scraper](jobSourceActor))

    val indexPageFlow: Flow[Scraper, IndexPage] = Flow[Scraper]
      .map(
        thisScraper => {
          println(s"IndexPageFlow: [${thisScraper.id.getOrElse(0)}] - url: ${thisScraper.sourceUrl}")
          IndexPage(scraper = thisScraper, content = Some(pageContent(thisScraper.sourceUrl)))
        }
      )

    val contentSink = ForeachSink[IndexPage] {
      el =>
        println(s"ContentSink: [${el.scraper.id.getOrElse(0)}] -  Content length: ${el.content.getOrElse("").length}")
        Thread.sleep(1000)
    }

    val loggerSink = ForeachSink[IndexPage] {
      el =>
        println(s"LoggerSink: [${el.scraper.id.getOrElse(0)}]")
    }

    val subscriber: Subscriber[IndexPage] = ActorSubscriber[IndexPage](system.actorOf(Props[IndexPageSubscriber], "IndexPageSubscriber"))

    val sinkB: Sink[IndexPage] = Sink(subscriber)

    val materialized = FlowGraph {
      implicit b =>
        import FlowGraphImplicits._
        val broadcast = Broadcast[IndexPage]
        src ~> indexPageFlow ~> broadcast ~> sinkB
        broadcast ~> contentSink
        broadcast ~> loggerSink
    }.run

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