package oen.bard2.youtube

case class Video(items: List[Item])
case class Item(snippet: Snippet, contentDetails: ContentDetails)
case class Snippet(title: String)
case class ContentDetails(duration: String)

object Video {
  import spray.json._
  import DefaultJsonProtocol._

  implicit val contentDetailsFormat = jsonFormat1(ContentDetails)
  implicit val snippetFormat = jsonFormat1(Snippet)
  implicit val itemFormat = jsonFormat2(Item)
  implicit val videoFormat = jsonFormat1(Video.apply)
}
