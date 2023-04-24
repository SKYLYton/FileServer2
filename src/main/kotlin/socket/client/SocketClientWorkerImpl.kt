package socket.client

import model.ServerOnlineModel
import socket.model.Offline
import com.google.gson.Gson
import socket.GsonManager
import socket.model.File
import socket.model.Online
import socket.model.ResponsePing
import state.SocketClientState

/**
 * @author Fedotov Yakov
 */
class SocketClientWorkerImpl constructor(
) : SocketClientWorker {

    val socket: SocketClient = SocketClient(this)

    private val gson = GsonManager.gson

    override fun startReceivingServerMessages(
        messageReceived: ((SocketClientState) -> Unit),
        onConnected: ((ServerOnlineModel) -> Unit),
        onDisconnected: (() -> Unit),
        clientStartingListener: ((isStart: Boolean, server: ServerOnlineModel?) -> Unit)
    ) {
        socket.messageReceived = messageReceived
        socket.onConnected = onConnected
        socket.onDisconnected = onDisconnected
        socket.clientStartingListener = clientStartingListener
    }

    override fun stopReceivingMessages() {
        socket.messageReceived = null
        socket.onConnected = null
        socket.onDisconnected = null
    }

    override fun isClientStart(): Boolean = socket.isStart()

    override fun startClient(address: String, name: String) {
        socket.start(address, name)
    }

    override fun stopClient() {
        stopReceivingMessages()
        socket.stop()
    }

    override fun sendOnline(name: String) {
        val message = gson.toJson(Online(name))
        socket.sendMessage(message)
    }

    override fun sendOffline() {
        val message = gson.toJson(Offline())
        socket.sendMessage(message)
    }

    override fun sendFile(name: String) {
        val file = java.io.File("C:/Users/yasha/Desktop/Работа/12.1/_ReferenceDatabase.zip")
        val message = gson.toJson(File(file.name, file.length().toInt()))
        socket.sendMessage(message)
        socket.sendFile(file)
    }

    override fun sendResponsePing() {
        val message = gson.toJson(ResponsePing())
        socket.sendMessage(message)
    }
}