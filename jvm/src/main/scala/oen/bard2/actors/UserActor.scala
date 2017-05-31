package oen.bard2.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import oen.bard2.{Data, Ping}
import oen.bard2.actors.UserActor.{Out, ToOut}

class UserActor(roomActor: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = emptyActor

  def emptyActor: Receive = {
    case Out(out) => context.become(handlingMessages(out))
  }

  def handlingMessages(out: ActorRef): Receive = {
    case toOut: ToOut =>
      out ! toOut.data

    case Ping =>

    case d: Data =>
      roomActor ! d

    case e => // TODO
      log.info(e.toString)
      out ! Ping
  }
}

object UserActor {
  def props(roomActor: ActorRef) = Props(new UserActor(roomActor))

  case class Out(out: ActorRef)
  case class ToOut(data: Data)
}
