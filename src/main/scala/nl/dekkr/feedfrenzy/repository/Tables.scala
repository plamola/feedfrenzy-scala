package nl.dekkr.feedfrenzy.repository

import nl.dekkr.feedfrenzy.model.ActionPhase.ActionPhase
import nl.dekkr.feedfrenzy.model.ActionType.ActionType
import nl.dekkr.feedfrenzy.model._

/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = scala.slick.driver.PostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: scala.slick.driver.JdbcProfile

  import org.joda.time.DateTime
  import scala.slick.driver.PostgresDriver.simple._
  import scala.slick.lifted.{ ProvenShape, ForeignKeyQuery }
  import com.github.tototoshi.slick.PostgresJodaSupport._

  class ArticleTable(tag: Tag) extends Table[Article](tag, "article") {
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val feedid: Column[Int] = column[Int]("feedid")
    val uri: Column[String] = column[String]("uri", O.Length(1024, varying = true))
    val link: Column[Option[String]] = column[Option[String]]("link", O.Length(1024, varying = true), O.Default(None))
    val title: Column[Option[String]] = column[Option[String]]("title", O.Length(255, varying = true), O.Default(None))
    val content: Column[Option[String]] = column[Option[String]]("content", O.Length(10485760, varying = true), O.Default(None))
    val author: Column[Option[String]] = column[Option[String]]("author", O.Length(255, varying = true), O.Default(None))
    val publisheddate: Column[Option[DateTime]] = column[Option[DateTime]]("publisheddate", O.Default(None))
    val updateddate: Column[Option[DateTime]] = column[Option[DateTime]]("updateddate", O.Default(None))
    val lastsynceddate: Column[Option[DateTime]] = column[Option[DateTime]]("lastsynceddate", O.Default(None))

    /** Uniqueness Index over (feedid, uri)  **/
    def idx = index("unique_feedid_uri", (feedid, uri), unique = true)

    def * : ProvenShape[Article] =
      (id.?, feedid.?, uri, link, title, content, author, publisheddate, updateddate, lastsynceddate) <> (Article.tupled, Article.unapply)

    def feed: ForeignKeyQuery[FeedTable, Feed] =
      foreignKey("feed_fk", feedid, TableQuery[FeedTable])(_.id)
  }
  lazy val articleTable = new TableQuery(tag => new ArticleTable(tag))

  class FeedTable(tag: Tag) extends Table[Feed](tag, "feed") {
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val feedurl: Column[String] = column[String]("feedurl", O.Length(1024, varying = true))
    val link: Column[Option[String]] = column[Option[String]]("link", O.Length(1024, varying = true), O.Default(None))
    val title: Column[Option[String]] = column[Option[String]]("title", O.Length(255, varying = true), O.Default(None))
    val description: Column[Option[String]] = column[Option[String]]("description", O.Length(1024, varying = true), O.Default(None))
    val copyright: Column[Option[String]] = column[Option[String]]("copyright", O.Length(255, varying = true), O.Default(None))
    val image: Column[Option[String]] = column[Option[String]]("image", O.Length(255, varying = true), O.Default(None))
    val publisheddate: Column[Option[DateTime]] = column[Option[DateTime]]("publisheddate", O.Default(None))
    val updateddate: Column[DateTime] = column[DateTime]("updateddate", O.Default(DateTime.now()))
    val updateInterval: Column[Int] = column[Int]("update_interval", O.Default(60))
    val nextupdate: Column[Long] = column[Long]("nextupdate", O.Default(DateTime.now().getMillis))
    val lastarticlecount: Column[Int] = column[Int]("lastarticlecount", O.Default(0))
    val faviconfk: Column[Int] = column[Int]("faviconfk", O.Default(0))
    val scraperid: Column[Option[Int]] = column[Option[Int]]("scraper_id", O.Default(None))

    /** Uniqueness Index over (feedurl) (database name feed_feedurl_key) */
    val index1 = index("feed_feedurl_key", feedurl, unique = true)

    def scraper: ForeignKeyQuery[ScraperTable, Scraper] =
      foreignKey("scraper_fk", scraperid, TableQuery[ScraperTable])(_.id)

    def * : ProvenShape[Feed] =
      (id.?, feedurl, link, title, description, copyright, image, publisheddate, updateddate, updateInterval, nextupdate, lastarticlecount, faviconfk, scraperid) <> (Feed.tupled, Feed.unapply)
  }
  lazy val feedTable = new TableQuery(tag => new FeedTable(tag))

  class ScraperTable(tag: Tag) extends Table[Scraper](tag, "scraper") {
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val url: Column[String] = column[String]("url", O.Length(1024, varying = true))
    val singlepage: Column[Boolean] = column[Boolean]("singlepage", O.Default(false))

    def * : ProvenShape[Scraper] =
      (id.?, url, singlepage) <> (Scraper.tupled, Scraper.unapply)
  }
  lazy val scraperTable = new TableQuery(tag => new ScraperTable(tag))

  class ScraperActionTable(tag: Tag) extends Table[ScraperAction](tag, "scraperaction") {
    val id: Column[Int] = column[Int]("action_id", O.AutoInc, O.PrimaryKey)
    val scraperid: Column[Int] = column[Int]("scraper_id")
    val actionPhase: Column[ActionPhase] = column[ActionPhase]("action_phase")
    val actionOrder: Column[Int] = column[Int]("action_order")
    val actionType: Column[ActionType] = column[ActionType]("action_type")
    val actionInput: Column[Option[String]] = column[Option[String]]("action_input_type", O.Length(1024, varying = true), O.Default(None))
    val actionTemplate: Column[Option[String]] = column[Option[String]]("action_template", O.Length(1024, varying = true), O.Default(None))
    val actionOutputVariable: Column[Option[String]] = column[Option[String]]("action_output_variable", O.Length(1024, varying = true), O.Default(None))
    val actionReplaceWith: Column[Option[String]] = column[Option[String]]("action_replace_with", O.Length(1024, varying = true), O.Default(None))

    def * : ProvenShape[ScraperAction] =
      (id.?, scraperid.?, actionPhase, actionOrder, actionType, actionInput, actionTemplate, actionOutputVariable, actionReplaceWith) <> (ScraperAction.tupled, ScraperAction.unapply)

    def scraper: ForeignKeyQuery[ScraperTable, Scraper] =
      foreignKey("scraper_fk", scraperid, TableQuery[ScraperTable])(_.id)

  }
  lazy val scraperActionTable = new TableQuery(tag => new ScraperActionTable(tag))

  class PageCacheTable(tag: Tag) extends Table[PageCache](tag, "page_cache") {
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val url: Column[String] = column[String]("url", O.Length(1024, varying = true))
    val content: Column[Option[String]] = column[Option[String]]("content", O.Length(10485760, varying = true), O.Default(None))
    val createdAt: Column[DateTime] = column[DateTime]("created_at", O.Default(DateTime.now()))

    def * : ProvenShape[PageCache] =
      (id.?, url, content, createdAt) <> (PageCache.tupled, PageCache.unapply)
  }
  lazy val pageCacheTable = new TableQuery(tag => new PageCacheTable(tag))

}