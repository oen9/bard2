package oen.bard2.components

import oen.bard2.{PlaylistPosition, Room}

class CacheData(
  var rooms: Set[Room] = Set(),
  var roomName: Option[String] = None,
  var playlist: Vector[PlaylistPosition] = Vector()
)
