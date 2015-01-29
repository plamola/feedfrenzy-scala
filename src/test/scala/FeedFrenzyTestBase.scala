import nl.dekkr.feedfrenzy.repository.{ Tables, Schema }

import scala.slick.driver.PostgresDriver.simple._
import org.scalatest.prop.Configuration
import org.specs2.mutable.Specification

import scala.slick.jdbc.meta.MTable
import com.typesafe.config.ConfigFactory
/**
 * Standard Test Base.
 */
trait FeedFrenzyTestBase extends Specification
    with Configuration {

  val spec = this

  args(sequential = true)
  implicit var session: Session = _
  implicit var conf = ConfigFactory.load

  def cleanDB(): Unit = {
    session = Schema.getSession
    dropDatabaseTables()
    Schema.createOrUpdate(session)
  }

  private def dropDatabaseTables(): Unit = {
    val existingTables = MTable.getTables.list
    if (existingTables.exists(_.name.name.equalsIgnoreCase("article"))) {
      Tables.articleTable.ddl.drop
    }
    if (existingTables.exists(_.name.name.equalsIgnoreCase("feed"))) {
      Tables.feedTable.ddl.drop
    }
    if (existingTables.exists(_.name.name.equalsIgnoreCase("scraperaction"))) {
      Tables.scraperActionTable.ddl.drop
    }
    if (existingTables.exists(_.name.name.equalsIgnoreCase("scraper"))) {
      Tables.scraperTable.ddl.drop
    }

  }

}