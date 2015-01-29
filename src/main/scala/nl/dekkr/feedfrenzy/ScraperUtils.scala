package nl.dekkr.feedfrenzy

import org.jsoup.Jsoup

/**
 * Author: matthijs
 * Created on: 29 Jan 2015.
 */
object ScraperUtils {

  import scala.collection.JavaConversions._

  def getIDs(content: String, selector: String): List[String] = {
    try {
      val doc = Jsoup.parse(content)
      val blockList = doc.select(selector).iterator.toList
      for { element <- blockList } yield element.parent().html()
    } catch {
      case e: Exception =>
        List.empty[String]
    }
  }
}
