package oen.bard2.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import oen.bard2.Ping
import oen.bard2.actors.UserActor.Out

class UserActor extends Actor with ActorLogging {

  override def receive: Receive = emptyActor

  def emptyActor: Receive = {
    case Out(out) => context.become(handlingMessages(out))
  }

  def handlingMessages(out: ActorRef): Receive = {
    case e => // TODO
      log.info(e.toString)
      out ! Ping
  }
}

object UserActor {
  def props = Props(new UserActor)

  case class Out(out: ActorRef)
}
