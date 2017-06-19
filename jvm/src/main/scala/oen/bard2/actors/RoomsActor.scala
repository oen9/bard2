package oen.bard2.actors

import akka.actor.{ActorRef, PoisonPill, Props, Terminated}
import akka.persistence.PersistentActor
import oen.bard2._
import oen.bard2.actors.RoomsActor._

class RoomsActor extends PersistentActor {

  var roomList: Set[RoomRef] = Set()
  val snapshotInterval = 1000

  override def receiveRecover: Receive = {
    case RoomCreated(name) => newRoom(name)
    case RoomDeleted(name) => deleteRoom(name)
    case RoomList(rooms) => rooms.foreach(newRoom)
  }

  override def receiveCommand: Receive = {
    case CreateRoom(r @ Room(roomName)) =>
      if (roomList.exists(_.name.equalsIgnoreCase(roomName))) {
        sender() ! RoomRejected(r)
      } else {
        sender() ! RoomAccepted(r)
        persist(RoomCreated(roomName))(_ => newRoom(roomName))
      }

    case GetRooms =>
      sender() ! Rooms(roomList.map(r => Room(r.name)))

    case CreateUser(roomName) =>
      roomList
        .find(_.name.equalsIgnoreCase(roomName))
        .fold {
          sender() ! RoomNotFound(Room(roomName))
        } (roomRef => {
          roomRef.actorRef ! RoomActor.CreateUser(sender())
        })

    case Terminated(terminated) =>
      roomList.find(_.actorRef == terminated).foreach(ter => {
        persist(RoomDeleted(ter.name))(_ => deleteRoom(ter.name))
      })
  }

  override def persistenceId: String = "rooms"

  def newRoom(name: String): Unit = changeState {
    val actorRef = context.actorOf(RoomActor.props(name))
    val roomRef = RoomRef(name, actorRef)
    context.watch(actorRef)

    roomList = roomList + roomRef
  }

  def deleteRoom(name: String): Unit = changeState {
    roomList.find(_.name == name).foreach(toDel => {
      roomList = roomList - toDel
      toDel.actorRef ! PoisonPill
      context.unwatch(toDel.actorRef)
    })
  }

  def changeState[A](f: => A): A = {
    val result = f
    if (lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0) {
      saveSnapshot(RoomList(roomList.map(_.name)))
    }
    result
  }
}

object RoomsActor {
  def props = Props(new RoomsActor)
  def name: String = "rooms"

  sealed trait Cmd
  case object GetRooms extends Cmd
  case class CreateUser(roomName: String) extends Cmd

  sealed trait Evt
  case class RoomCreated(name: String) extends Evt
  case class RoomDeleted(name: String) extends Evt
  case class RoomList(rooms: Set[String]) extends Evt

  case class RoomRef(name: String, actorRef: ActorRef)
}
