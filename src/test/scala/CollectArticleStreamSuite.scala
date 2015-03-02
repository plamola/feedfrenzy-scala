import nl.dekkr.feedfrenzy.repository.Schema
import nl.dekkr.feedfrenzy.model.Syndication

/**
 * Test suite for the collect article stream
 */
class CollectArticleStreamSuite extends FeedFrenzyTestBase {

  " CollectArticleStreamSuite" should {

    "Have a feed which in None" in {
      val feed = None
      feed must be equalTo None
    }

  }

}