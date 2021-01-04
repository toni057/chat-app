//package quickstart.akka
//
//import akka.actor.typed.ActorSystem
//import quickstart.GreeterMain
//import quickstart.GreeterMain.SayHello
//
////#main-class
//object AkkaQuickstart extends App {
//  //#actor-system
//  val greeterMain: ActorSystem[GreeterMain.SayHello] = ActorSystem(GreeterMain(), "AkkaQuickStart")
//  //#actor-system
//
//  //#main-send-messages
//  greeterMain ! SayHello("Charles")
//  //#main-send-messages
//}
