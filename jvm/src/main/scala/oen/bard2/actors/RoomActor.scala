package oen.bard2.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import oen.bard2.actors.RoomActor.CreateUser

class RoomActor extends Actor with ActorLogging {

  var users: Set[ActorRef] = Set()

  override def receive: Receive = {
    case CreateUser(receiver) =>
      val user = context.actorOf(UserActor.props)
      context.watch(user)
      users = users + user
      receiver ! user

    case Terminated(terminated) =>
      users = users - terminated

    case e =>
      log.info(e.toString)  // TODO
  }
}

object RoomActor {
  def props = Props(new RoomActor)

  case class CreateUser(receiver: ActorRef)
}
