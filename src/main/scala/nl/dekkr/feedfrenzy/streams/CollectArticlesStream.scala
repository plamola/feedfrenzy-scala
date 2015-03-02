package nl.dekkr.feedfrenzy.streams

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import nl.dekkr.feedfrenzy.ScraperUtils
import nl.dekkr.feedfrenzy.model._
import nl.dekkr.feedfrenzy.streams.flows.{GetPageContent, SplitIndexIntoBlocks}

/**
 * Author: Matthijs Dekker
 * Created on: 26 Jan 2015.
 */
object CollectArticlesStream {

  def runWithSource(src: Source[ScraperDefinition]): Unit = {
    implicit val system = ActorSystem("feedfrenzy-collector")
    implicit val materializer = FlowMaterializer()

    println("########################################################################################################################")

    //val publisher: Publisher[ContentBlock] = ActorPublisher[ContentBlock](system.actorOf(Props[ScraperActorPublisher], "JobSource"))
    //val subscriber: Subscriber[ContentBlock] = ActorSubscriber[ContentBlock](system.actorOf(Props[IndexPageSubscriber], "IndexPageSubscriber"))

    // val core = new ScraperCore(new ScraperRepositoryDummyComponent)

    val scraperToContentBlock: Flow[ScraperDefinition, ContentBlock] = Flow[ScraperDefinition]
      .map(scraperDef => new ContentBlock(scraperDefinition = scraperDef, sourceUrl = Some(scraperDef.scraper.sourceUrl)))

    val fetchPage: Flow[ContentBlock, ContentBlock] = Flow[ContentBlock]
      .transform(() => new GetPageContent())

    val splitIntoBlocks: Flow[ContentBlock, ContentBlock] = Flow[ContentBlock]
      .transform(() => new SplitIndexIntoBlocks())

    val generateUID: Flow[ContentBlock, ContentBlock] = Flow[ContentBlock]
      .map(block => ScraperUtils.scrape(block, ActionPhase.UIDS)).filter(p => p.result != None)

    val filterOldArticles: Flow[ContentBlock, ContentBlock] = Flow[ContentBlock]
      .map(block => block).filter(p => p.result != None && p.result.get.articleUid.getOrElse("") == "http://www.rtvutrecht.nl/nieuws/1292699")

    val prepareNextPageFetch: Flow[ContentBlock, ContentBlock] = Flow[ContentBlock]
      .map(
        block =>
          if (block.scraperDefinition.scraper.singlePage) block
          else ContentBlock(scraperDefinition = block.scraperDefinition, sourceUrl = block.result.get.articleUrl, result = block.result)
      )

    val generateArticles: Flow[ContentBlock, ContentBlock] = Flow[ContentBlock]
      .map(block => ScraperUtils.scrape(block, ActionPhase.CONTENT)).filter(p => p.result != None)

    val printSink = ForeachSink[ContentBlock] {
      value =>
        println(s"PrintSink: [${value.scraperDefinition.scraper.id.get}] [${value.pageContent.get.length}]")
        for {actions <- value.scraperDefinition.actions} println(s"[${actions.action_id.get} \t${actions.actionPhase}]  \t${actions.action_order} \t: ${actions.actionType} \t${actions.actionTemplate}\t${actions.actionOutputVariable}")
    }

    val resultSink = ForeachSink[ContentBlock] {
      el =>
        println(s"ResultSink: [${el.scraperDefinition.scraper.id.getOrElse(0)}] -  Content length: ${el.pageContent.getOrElse("").length}")
    }

    val blockSink = ForeachSink[ContentBlock] {
      value => {
        println(s" ==== BlockSink: [${value.scraperDefinition.scraper.id.get}] \t [${value.result.get.articleUid.getOrElse("---")}] \t [${value.result.get.articleUrl.getOrElse("---")}] ====")
        if (value.result != None) {
          println(s"BlockSink: [${value.scraperDefinition.scraper.id.get}] \t title \t [${value.result.get.articleTitle.getOrElse("---")}]")
          println(s"BlockSink: [${value.scraperDefinition.scraper.id.get}] \t author \t[${value.result.get.articleAuthor.getOrElse("---")}]")
          println(s"BlockSink: [${value.scraperDefinition.scraper.id.get}] \t length \t[${value.result.get.articleContent.getOrElse("").length}]")
        }
      }
    }

    val materialized = FlowGraph {
      implicit b =>
        import akka.stream.scaladsl.FlowGraphImplicits._
        val broadcast = Broadcast[ContentBlock]
        src ~> scraperToContentBlock ~> fetchPage ~>
               broadcast ~> printSink
               broadcast ~> splitIntoBlocks ~> generateUID ~> filterOldArticles ~>
                 prepareNextPageFetch ~> fetchPage ~> generateArticles ~> blockSink

    }.run

    //        import system.dispatcher
    //        materialized.get(resultSink).onComplete {
    //          case Success(_) =>
    //            //Try(output.close())
    //            system.shutdown()
    //          case Failure(e) =>
    //            println(s"Failure: ${e.getMessage}")
    //            //Try(output.close())
    //            system.shutdown()
    //        }
  }

}
