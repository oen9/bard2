package oen.bard2.youtube

case class SearchResults(prevPageToken: Option[String], nextPageToken: Option[String], results: Vector[SearchResult])
case class SearchResult(title: String, thumbnail: String, videoId: String)
