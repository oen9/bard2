package oen.bard2.components

import oen.bard2.Room

class CacheData(
  var rooms: Set[Room] = Set(),
  var roomName: Option[String] = None
)
