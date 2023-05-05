package socket.server

import state.SocketServerState
import com.google.gson.Gson
import socket.model.*

/**
 * @author Fedotov Yakov
 */
class SocketServerWorkerImpl(
    private val gson: Gson
) : SocketServerWorker {

    private val socket = SocketServer(this)

    override fun startReceivingServerMessages(
        messageReceived: (SocketServerState) -> Unit,
        userConnected: (User) -> Unit,
        userDisconnected: (User) -> Unit,
        serverStartingListener: ((isStart: Boolean, ip: String) -> Unit)
    ) {
        socket.messageReceived = messageReceived
        socket.userConnected = userConnected
        socket.userDisconnected = userDisconnected
        socket.serverStartingListener = serverStartingListener
    }

    override fun startGettingTime(
        timeListener: (Int) -> Unit,
        savedProcessListener: (progress: Int) -> Unit
    ) {
        socket.fileSaved = timeListener
        socket.savedProcessListener = savedProcessListener
    }

    override fun stopReceivingMessages() {
        socket.messageReceived = null
        socket.userConnected = null
        socket.userDisconnected = null
    }

    override fun isServerStart(): Boolean = socket.isStart()

    override fun startServer(name: String) {
        socket.start(name)
    }

    override fun stopServer() {
        stopReceivingMessages()
        socket.stop()
    }

    override fun sendServerOnline() {
        val message = gson.toJson(ServerOnline())
        socket.sendMessage(message)
    }

    override fun sendPing() {
        val message = gson.toJson(RequestPing())
        socket.sendMessage(message)
    }

    override fun sendFile(fileName: String) {
        val file = java.io.File(fileName)
        val message = gson.toJson(File(file.name, file.length().toInt()))
        socket.sendMessage(message)
    }

    override fun sendFile(file: java.io.File) {
        val fileModel = File(file.name, file.length().toInt())

        val message = gson.toJson(fileModel)
        socket.sendMessage(message)
    }

    override fun sendFiles(files: List<java.io.File>) {
        socket.sendFiles(files)
    }

}