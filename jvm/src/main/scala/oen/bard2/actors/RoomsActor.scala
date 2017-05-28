package oen.bard2.actors

import akka.actor.{ActorRef, Props}
import akka.persistence.PersistentActor
import oen.bard2._
import oen.bard2.actors.RoomsActor._

class RoomsActor extends PersistentActor {

  var roomList: Set[RoomRef] = Set()
  val snapshotInterval = 1000

  override def receiveRecover: Receive = {
    case RoomCreated(name) => newRoom(name)
    case RoomList(rooms) => rooms.foreach(newRoom)
  }

  override def receiveCommand: Receive = {
    case CreateRoom(r @ Room(name)) =>
      if (roomList.exists(_.name.equalsIgnoreCase(name))) {
       sender() ! RoomRejected(r)
      } else {
        sender() ! RoomAccepted(r)
        persist(RoomCreated(name))(_ => newRoom(name))
      }

    case GetRooms =>
      sender() ! Rooms(roomList.map(r => Room(r.name)))
  }

  override def persistenceId: String = "rooms"

  def newRoom(name: String): Unit = {
    val actorRef = context.actorOf(RoomActor.props)
    val roomRef = RoomRef(name, actorRef)

    roomList = roomList + roomRef

    if (lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0) {
      saveSnapshot(RoomList(roomList.map(_.name)))
    }
  }
}

object RoomsActor {
  def props = Props(new RoomsActor)
  def name: String = "rooms"

  sealed trait Cmd
  case object GetRooms extends Cmd

  sealed trait Evt
  case class RoomCreated(name: String) extends Evt
  case class RoomDeleted(name: String) extends Evt
  case class RoomList(rooms: Set[String]) extends Evt

  case class RoomRef(name: String, actorRef: ActorRef)
}
