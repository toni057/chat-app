# Chat program

To open port 4000 (machine running the server):
- sudo ufw allow 4000

Run server:
- java -cp target/scala-2.13/akka-chat-assembly-0.1.jar quickstart.sockets.ServerMain ip port

Run client: 
- java -cp target/scala-2.13/akka-chat-assembly-0.1.jar quickstart.sockets.ClientMain toni sil op port



TODO:
- notification when user comes online
- if the server is down implement reconnect strategy (eg exponential)
- heartbeat to periodically check if users are online
- distributed


DONE: 
- notification when user goes offline
- notification if user is offline
