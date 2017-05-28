package oen.bard2.actors

import akka.actor.{Actor, ActorLogging, Props}

class RoomActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case e => log.info(e.toString)
  }
}

object RoomActor {
  def props = Props(new RoomActor)
}
