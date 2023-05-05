package socket.server

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.toModel
import socket.BaseSocket
import socket.model.*
import socket.model.File
import state.SocketServerState
import java.io.*
import java.net.Socket
import java.nio.charset.StandardCharsets


/**
 * @author Fedotov Yakov
 */
class UserManager(
    var socket: Socket?
) : BaseSocket() {
    var userConnected: ((connectedUser: User) -> Unit)? = null
    var userDisconnected: ((userManager: UserManager) -> Unit)? = null
    var messageReceived: ((message: SocketServerState) -> Unit)? = null

    var user = User()
        private set
    private var bufferSender: PrintWriter? = null
    private var bufferInput: BufferedReader? = null

    private var running = false

    private val mutex = Mutex()

    private var startTime = System.currentTimeMillis()


    private fun runSocket() {
        running = true
        doWork {
            // отправляем сообщение клиенту
            bufferSender = runCatching {
                    PrintWriter(
                        BufferedWriter(
                            OutputStreamWriter(
                                socket?.getOutputStream(),
                                StandardCharsets.UTF_8
                            )
                        ),
                        true
                    )
            }.getOrNull()

            // читаем сообщение от клиента
            bufferInput = runCatching {
                    BufferedReader(InputStreamReader(socket?.getInputStream(), StandardCharsets.UTF_8))
            }.getOrNull()

            bufferInput?.let {
                println("Юзер сохранен")
                while (running) {
                    runCatching {
                        processSocket(it)
                    }.onFailure {
                        println("Error123" + it.message ?: "")
                    }
                }
            }

            running = false
        }
    }

    private suspend fun processSocket(input: BufferedReader) {
        val message: String?
        mutex.withLock {
            message = kotlin.runCatching { input.readLine() }.getOrNull()
        }

        fetchCommand(message)?.let {
            if (!processReceived(it)) {
                messageReceived?.invoke(it)
            }
        }
    }

    private fun processReceived(message: SocketServerState): Boolean {
        when (message) {
            is SocketServerState.Offline -> {
                close()
            }

            is SocketServerState.Online -> {
                user = User(message.online.name, user.id)
                userConnected?.invoke(user)
            }

            else -> return false
        }
        return true
    }

    private suspend fun fetchCommand(message: String?): SocketServerState? {
        if (message.isNullOrEmpty()) {
            return null
        }
        //Log.e("TAG", "Сообщение: $message")
        var command: BaseCommand = gson.fromJson(message, Command::class.java)

        val socketState: SocketServerState

        when ((command as? Command)?.toModel?.commandType) {
            CommandType.ONLINE -> {
                command = gson.fromJson(message, Online::class.java)
                socketState = SocketServerState.Online(command.toModel)
            }

            CommandType.OFFLINE -> {
                command = gson.fromJson(message, Offline::class.java)
                socketState = SocketServerState.Offline(command.toModel)
            }

            CommandType.FILE -> {
                command = gson.fromJson(message, File::class.java)
                socketState = SocketServerState.File(command.toModel)
                mutex.withLock {
                    //receiveFile(socketState.file.data, socketState.file.size)
                }
            }

            else -> {
                processReceivedWithoutUi(command)
                return null
            }
        }

        return socketState
    }



    private fun processReceivedWithoutUi(command: BaseCommand) {
        println("${user.id} юзер пинганул")
        when ((command as? Command)?.toModel?.commandType) {
            CommandType.RESPONSE_PING -> {
                println("${user.id} юзер пинганул")
            }

            else -> return
        }
    }

    fun open(id: Int) {
        user = User(id = id)
        runSocket()
    }

    fun close() {
        running = false
        kotlin.runCatching {
            bufferSender?.let {
                it.flush()
                it.close()
            }
        }
        bufferSender = null
        kotlin.runCatching {
            socket?.close()
        }
        socket = null
        userDisconnected?.invoke(this)
    }

    fun isActive() = System.currentTimeMillis() - startTime <= 60000

    fun isConnected() =
        socket?.isConnected == true && System.currentTimeMillis() - startTime <= 90000 && running

    fun sendMessage(message: String?) {
        bufferSender?.let {
            if (!it.checkError()) {
                it.println(message)
                it.flush()
            }
        }
    }
}