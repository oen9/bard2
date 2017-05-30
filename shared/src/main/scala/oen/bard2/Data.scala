package oen.bard2

import derive.key

sealed trait Data

@key("room") case class Room(name: String) extends Data
@key("room-list") case class Rooms(rooms: Set[Room]) extends Data
@key("create-room") case class CreateRoom(room: Room) extends Data
@key("delete-room") case class DeleteRoom(room: Room) extends Data
@key("room-accepted") case class RoomAccepted(room: Room) extends Data
@key("room-rejected") case class RoomRejected(room: Room) extends Data
@key("room-not-found") case class RoomNotFound(room: Room) extends Data

@key("ping") case object Ping extends Data

object Data {
  def toJson(data: Data): String = {
    upickle.default.write(data)
  }

  def fromJson(json: String): Data = {
    upickle.default.read[Data](json)
  }
}
