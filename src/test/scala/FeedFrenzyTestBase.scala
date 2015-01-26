import akka.actor.{ ActorSystem, ActorRefFactory }
import com.typesafe.config.ConfigFactory
import nl.dekkr.feedfrenzy.db.{ Tables, Schema }

import scala.slick.driver.PostgresDriver.simple._
import org.scalatest.prop.Configuration
import org.specs2.mutable.Specification

import scala.slick.jdbc.meta.MTable
import com.typesafe.config.ConfigFactory

import scala.slick.jdbc.meta.MTable
import com.typesafe.config.ConfigFactory
/**
 * Standard Test Base.
 */
trait FeedFrenzyTestBase extends Specification
    //with Specs2RouteTest with HttpService
    with Configuration {

  val spec = this
  //  val restService = new RestService {
  //    override implicit def actorRefFactory: ActorRefFactory = spec.actorRefFactory
  //  }.myRoute

  args(sequential = true)
  implicit var session: Session = _
  implicit var conf = ConfigFactory.load

  // connects the DSL to the test ActorSystem
  //implicit def actorRefFactory: ActorSystem = system

  def cleanDB(): Unit = {
    session = Schema.getSession
    dropDatabaseTables()
    Schema.createOrUpdate(session)
  }

  private def dropDatabaseTables(): Unit = {
    val existingTables = MTable.getTables.list
    if (existingTables.exists(_.name.name.equalsIgnoreCase("scraper"))) {
      Tables.scraperTable.ddl.drop
    }
    if (existingTables.exists(_.name.name.equalsIgnoreCase("article"))) {
      Tables.articleTable.ddl.drop
    }
    if (existingTables.exists(_.name.name.equalsIgnoreCase("feed"))) {
      Tables.feedTable.ddl.drop
    }
  }

}