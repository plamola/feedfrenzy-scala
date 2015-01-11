package nl.dekkr.feedfrenzy

import nl.dekkr.feedfrenzy.db.{ Tables, Schema }
import nl.dekkr.feedfrenzy.model.Source

import scala.slick.driver.PostgresDriver.simple._

/**
 * Author: Matthijs
 * Created on: 11 Jan 2015.
 */
object IndexSource {

  def main(args: Array[String]): Unit = {

    try {
      val sources = TableQuery[Tables.SourceTable]
      implicit val session = Schema.getSession
      Schema.createOrUpdate(session)

      // Add some dummy feeds
      if (sources.list.size < 1)
        sources += Source(sourceurl = "http://nu.nl")

      if (sources.list.size < 2)
        sources += Source(sourceurl = "http://dagartikel.nl")

      // List all available feeds
      println("id \tupdated \t \t \turl")
      for { c <- sources.list } println("" + c.id.get + " \t" + c.updateddate + " \t" + c.sourceurl)
      println("##### Done")
    } catch {
      case e: Exception => println(s"ERROR: ${e.getMessage} [${e.getCause}}]")
    }
  }
}