import model.ServerOnlineModel
import socket.client.SocketClient
import socket.client.SocketClientWorker
import socket.client.SocketClientWorkerImpl

fun main(args: Array<String>) {

    var isRunning = true;

    val socketClientWorker = SocketClientWorkerImpl()
    socketClientWorker.startClient("192.168.10.11", "name")
    socketClientWorker.startReceivingServerMessages({
        println(it.toString())
    }, {
        println("connected")

        socketClientWorker.sendFile("")
    }, {
        println("disconnected")
        isRunning = false

    }, { isStart: Boolean, server: ServerOnlineModel? ->
        println(server?.command)
    })

    while (isRunning){

    }
}