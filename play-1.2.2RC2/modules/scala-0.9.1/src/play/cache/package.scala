package play {

    package object cache {

        // Override the Java play.cache.Cache API
        val Cache = play.cache.ScalaCache

    }

}
