package nl.dekkr.feedfrenzy

import akka.stream.scaladsl._
import nl.dekkr.feedfrenzy.model._
import nl.dekkr.feedfrenzy.streams.CollectArticlesStream

/**
 * Author: Matthijs
 * Created on: 11 Jan 2015.
 */

object IndexSource extends Scrapers with ScraperRepositoryDbComponent {
  //lazy val scraperRepository = new ScraperRepositoryDbComponent {}

  def main(args: Array[String]): Unit = {
    CollectArticlesStream.runWithSource(Source(this.getUpdatable))
  }

}

