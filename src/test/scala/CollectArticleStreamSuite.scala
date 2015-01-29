import nl.dekkr.feedfrenzy.repository.Schema
import nl.dekkr.feedfrenzy.model.Syndication

/**
 * Created by Matthijs Dekker on 26/01/15.
 */
class CollectArticleStreamSuite extends FeedFrenzyTestBase {

  " CollectArticleStreamSuite" should {

    "Have a feed which in None" in {
      val feed = None
      feed must be equalTo None
    }

  }

}