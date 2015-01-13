package nl.dekkr.feedfrenzy.db

import com.typesafe.config.{ Config, ConfigFactory }

import scala.slick.jdbc.meta.MTable
import scala.slick.driver.PostgresDriver.simple._

/**
 * Author: matthijs
 * Created on: 31 Aug 2014.
 */
object Schema {

  def getSession: Session = getConfiguredSession(ConfigFactory.load)

  def createOrUpdate(implicit session: Session) {
    val existingTables = MTable.getTables.list
    if (existingTables.isEmpty) {
      createAll
    } else {
      if (!existingTables.exists(_.name.name.equalsIgnoreCase("scraper"))) {
        Tables.scraperTable.ddl.create
      } else {
        // Update existing table structure
      }
      //      if (!existingTables.exists(_.name.name.equalsIgnoreCase("indexpage"))) {
      //        Tables.indexpageTable.ddl.create
      //      } else {
      //        // Update existing table structure
      //      }

      if (!existingTables.exists(_.name.name.equalsIgnoreCase("feed"))) {
        Tables.feedTable.ddl.create
      } else {
        // Update existing table structure
      }
      if (!existingTables.exists(_.name.name.equalsIgnoreCase("article"))) {
        Tables.articleTable.ddl.create
      } else {
        // Update existing table structure
      }

    }
  }

  def createAll(implicit session: Session): Unit = {
    Tables.scraperTable.ddl.create
    Tables.feedTable.ddl.create
    Tables.articleTable.ddl.create
    //    Tables.indexpageTable.ddl.create
  }

  private def getConfiguredSession(conf: Config) = {
    Database.forURL(
      url = conf.getString("hoppr.database.url"),
      user = conf.getString("hoppr.database.user"),
      password = conf.getString("hoppr.database.password"),
      driver = conf.getString("hoppr.database.driver")).createSession()
  }

}
