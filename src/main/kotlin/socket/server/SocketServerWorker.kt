package socket.server

import socket.model.User
import state.SocketServerState
import java.io.File

/**
 * @author Fedotov Yakov
 */
interface SocketServerWorker {
    fun startReceivingServerMessages(
        messageReceived: ((SocketServerState) -> Unit),
        userConnected: (User) -> Unit,
        userDisconnected: ((User) -> Unit),
        serverStartingListener: ((isStart: Boolean, ip: String) -> Unit)
    )

    fun startGettingTime(
        timeListener: (Int) -> Unit,
        savedProcessListener: ((progress: Int) -> Unit)
    )

    fun stopReceivingMessages()
    fun isServerStart(): Boolean
    fun startServer(name: String)
    fun stopServer()

    fun sendServerOnline()

    fun sendPing()

    fun sendFile(fileName: String)

    fun sendFile(file: File)

    fun sendFiles(file: List<File>)
}