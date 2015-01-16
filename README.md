FeedFrenzy - The Scala version
==============================
FeedFrenzy is an application which publishes RSS feeds it has created by scraping other sites.

Often website do not publish an RSS feed them selfs, or they publish only a short summary of articles.


###History
The original version of FeedFrenzy was written in Ruby on Rails, version 0.9.2 and over the years received some upgrades minor upgrades.
After a serious security flaw was discovered in all RoR versions, an upgrade to a new RoR version was necessary.
However, at that time, it made more sense to rewrite the whole application.
There where some stability issues which needed resolving and also from a usabilty standpoint things could be better.

The second version of FeedFrenzy was build using the Java version of the Play framework and has been running for almost 2 years without any problems.

This third iteration will be written in Scala and will be made (for the first time) open source.


###Technology
The backend will use an implementation of [Reactive Streams](http://www.reactive-streams.org/), Akka Streams.

The front-end will most likely by written in AngularJS, which talks to Spray or Akka Http endpoints.


