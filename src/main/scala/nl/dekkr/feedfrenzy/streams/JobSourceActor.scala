package nl.dekkr.feedfrenzy.streams

import akka.stream.actor.ActorPublisher
import nl.dekkr.feedfrenzy.db.{ Schema, Tables }
import nl.dekkr.feedfrenzy.model.{ Feed, Scraper }
import org.joda.time.DateTime

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery

case class Kick()

/**
 * Created by Matthijs Dekker on 13/01/15.
 */
class JobSourceActor extends ActorPublisher[Scraper] {

  import scala.concurrent.duration._

  implicit val ec = context.dispatcher

  //val getNewJobs = context.system.scheduler.schedule(10 seconds, 1 minute, self, NewJobListing)
  val kick = context.system.scheduler.schedule(1 second, 1 second, self, Kick())

  var scrapers = List.empty[Scraper]

  override def receive: Receive = {
    case Kick() => {
      if (scrapers.isEmpty || scrapers.length == 0) {
        scrapers = getUpdatableFeeds.map(feed => Scraper(id = feed.id, sourceUrl = feed.feedurl, singlePage = true))
        println(s"[${DateTime.now}] JobSourceActor got new jobs - ${scrapers.length} / $totalDemand")
      }

      if (scrapers.length > 0 && totalDemand > 0) {
        println(s"[${DateTime.now}] JobSourceActor got kicked - ${scrapers.length} / $totalDemand")
        val (use, keep) = scrapers.splitAt(totalDemand.toInt)
        scrapers = keep
        use foreach onNext
      }
    }
    //    case NewJobListing() => {
    //      println("NewJobs")
    //      //      if (scrapers.isEmpty || scrapers.length == 0) {
    //      //        scrapers = getUpdatableFeeds.map(feed => Scraper(id = feed.id, sourceUrl = feed.feedurl, singlePage = true))
    //      //        println(s"[${DateTime.now}] JobSourceActor got new jobs - ${scrapers.length} / $totalDemand")
    //      //      }
    //    }
  }

  override def postStop() = {
    kick.cancel()
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

      if (feeds.list.size < 3)
        feeds += Feed(feedurl = "http://news.google.com")

      if (feeds.list.size < 4)
        feeds += Feed(feedurl = "http://news.yahoo.com")

      if (feeds.list.size < 5)
        feeds += Feed(feedurl = "http://www.theverge.com/")

      if (feeds.list.size < 6)
        feeds += Feed(feedurl = "http://news.ycombinator.com/")

      feeds.list

    } catch {
      case e: Exception =>
        println(s"ERROR: ${e.getMessage} [${e.getCause}}]")
        List.empty
    }
  }

}