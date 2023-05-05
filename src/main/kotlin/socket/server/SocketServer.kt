package socket.server

import state.SocketServerState
import kotlinx.coroutines.delay
import socket.BaseSocket
import socket.model.ServerOnline
import socket.model.User
import socket.model.toJson
import java.io.File
import java.net.ServerSocket
import java.net.Socket

/**
 * @author Fedotov Yakov
 */
class SocketServer constructor(private val socketWorker: SocketServerWorker) :
    BaseSocket() {

    private var files: MutableList<File> = mutableListOf()
    private var running = false
        set(value) {
            field = value
            doWorkInMainThread {
                serverStartingListener?.invoke(value, serverSocket?.inetAddress.toString())
            }
        }
    private var serverSocket: ServerSocket? = null
    private var user: UserManager? = null
    private val fileUsers: MutableList<FileUserManager> = mutableListOf()
    var userConnected: ((user: User) -> Unit)? = null
    var userDisconnected: ((user: User) -> Unit)? = null
    var messageReceived: ((SocketServerState) -> Unit)? = null
    var serverStartingListener: ((isStart: Boolean, ip: String) -> Unit)? = null
    var fileSaved: ((second: Int) -> Unit)? = null
    var savedProcessListener: ((progress: Int) -> Unit)? = null

    private var nameServer = "Server"

    fun isStart() = running

    private fun runServer() {
        doWork {
            runCatching {
                serverSocket = ServerSocket(5656)
                running = true

                while (running) {
                    runCatching {
                        processSocket()
                    }.onFailure {
                        it.message
                    }
                }
                running = false
            }.onFailure {
                it.message
            }
        }
        doWork {
            while (running) {
                if (user == null) {
                    delay(30000)
                    continue
                }

                user?.takeIf { !it.isActive() }?.close()

                if (running) {
                    delay(10000)
                }
            }
        }
    }

    private fun processSocket() {
        val client = serverSocket?.accept()
        client?.let {
            if (user != null) {
                saveFileUser(it)
            } else {
                saveUser(it)
            }
        }
    }

    private fun saveFileUser(socket: Socket) {
        FileUserManager(socket).apply {
            open(files[0])
            this@SocketServer.fileUsers.add(this)
            messageReceived = {

            }
            fileReceive = { isStart ->
                if (isStart) {
                    files.removeAt(0)
                    sendFiles(files)
                } else {
                    close()
                    this@SocketServer.fileUsers.remove(this)
                }
            }
        }
    }

    private fun saveUser(socket: Socket) {
        UserManager(socket).apply {
            open(socket.port)
            this@SocketServer.user = this
            userConnected = {
                socketWorker.sendServerOnline()
                doWorkInMainThread {
                    this@SocketServer.userConnected?.invoke(it)
                }
            }
            userDisconnected = {
                doWorkInMainThread {
                    this@SocketServer.userDisconnected?.invoke(it.user)
                }
                removeUser()
            }
            messageReceived = { message ->
                processReceived(message)
                doWorkInMainThread {
                    this@SocketServer.messageReceived?.invoke(message)
                }
            }
        }
    }

    fun sendFiles(files: List<File>) {
        this.files = files.toMutableList()
        if (files.isEmpty()) {
            return
        }
        println("123")
        doWork {
            socketWorker.sendFile(files[0])
        }
    }

    private fun processReceived(message: SocketServerState) {
        when (message) {
            is SocketServerState.OnlineTest -> {
                sendMessage(ServerOnline().toJson)
            }

            is SocketServerState.File -> {



            }

            else -> return
        }
    }

    private fun removeUser() {
        user = null
    }

    fun sendMessage(message: String) {
        user?.sendMessage(message)
    }

    fun start(name: String) {
        if (running) {
            return
        }
        if (name.isNotEmpty()) {
            this.nameServer = name
        }
        runServer()
    }

    fun stop() {
        doWork {
            fileUsers.forEach {
                it.close()
            }
            fileUsers.clear()
            runCatching {
                user?.close()
            }
            user = null
            running = false
            // закрытие сервера
            kotlin.runCatching {
                serverSocket?.close()
            }
            serverSocket = null
        }
    }
}