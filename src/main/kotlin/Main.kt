import com.google.gson.Gson
import model.ServerOnlineModel
import socket.client.SocketClient
import socket.client.SocketClientWorker
import socket.client.SocketClientWorkerImpl
import socket.server.SocketServer
import socket.server.SocketServerWorkerImpl
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger

fun main(args: Array<String>) {

    var isRunning = true;

    /*    val socketClientWorker = SocketClientWorkerImpl()
        socketClientWorker.startClient("192.168.42.73", "name")
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
        })*/

    ////////////////////////////////////////////////

    val sWorker = SocketServerWorkerImpl(Gson());
    sWorker.startServer("123")

    val list = mutableListOf<File>()
    list.add(File("C:/Users/yasha/Desktop/Работа/12.1/_ReferenceDatabase.zip"))
    //list.add(File("C:/Users/yasha/Desktop/Работа/12.1/_SecurityDatabase.zip"))

    sWorker.startReceivingServerMessages({

    }, {
        sWorker.sendFiles(list)

    }, {

    }) { isStart: Boolean, ip: String ->

    }

    while (isRunning) {

    }

}
