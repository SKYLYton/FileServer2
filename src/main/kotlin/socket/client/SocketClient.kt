package socket.client

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.ServerOnlineModel
import model.toModel
import socket.BaseSocket
import socket.model.*
import socket.model.File
import state.SocketClientState
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets


/**
 * @author Fedotov Yakov
 */
class SocketClient constructor(
    private val socketWorker: SocketClientWorker
) :
    BaseSocket() {

    private var server: ServerOnlineModel? = null

    private var address: String = ""
    private var name: String = "User"

    var messageReceived: ((SocketClientState) -> Unit)? = null
    var onConnected: ((server: ServerOnlineModel) -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var clientStartingListener: ((isStart: Boolean, server: ServerOnlineModel?) -> Unit)? = null
        set(value) {
            field = value
            field?.invoke(mRun, server)
        }

    private var mServerMessage: String? = null
    private var mRun = false // флаг, определяющий, запущен ли сервер
        set(value) {
            field = value
            if (!value) {
                server = null
            }
            doWorkInMainThread {
                clientStartingListener?.invoke(value, server)
            }
        }

    private var startTime = System.currentTimeMillis()
    private val mutex = Mutex()


    private var mBufferOut: PrintWriter? = null
    private var mBufferIn: BufferedReader? = null
    private var socket: Socket? = null

    fun isStart() = mRun

    fun start(address: String, name: String) {
        if (mRun) {
            return
        }
        this.address = address
        if (name.isNotEmpty()) {
            this.name = name
        }
        runClient()
    }

    private fun runClient() {
        mRun = true
        doWork {
            runCatching {
                socket = Socket(address, 5656)

                // отправляем сообщение клиенту
                mBufferOut = runCatching {
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
                mBufferIn = runCatching {
                    BufferedReader(
                        InputStreamReader(
                            socket?.getInputStream(),
                            StandardCharsets.UTF_8
                        )
                    )
                }.getOrNull()

                mBufferIn?.let {
                    socketWorker.sendOnline(name)
                    onConnected?.invoke(ServerOnlineModel())
                    while (mRun) {
                        runCatching {
                            processSocket(it)
                        }.onFailure {
                        }
                    }
                }
            }.onFailure {
                it.message
            }.also {
                runCatching {
                    socket.takeIf { it != null && it.isConnected }?.close()
                }
            }
            mRun = false
            onDisconnected?.invoke()
        }

        doWork {
            while (mRun) {
                delay(10000)
                if (!mRun) {
                    break
                }
                mutex.withLock {
                    if (System.currentTimeMillis() - startTime >= 30000) {
                        socketWorker.sendResponsePing()
                    }
                }
            }
        }
    }

    private fun processSocket(input: BufferedReader) {
        val message = kotlin.runCatching { input.readLine() }.onFailure {
        }.getOrNull()

        if (message.isNullOrEmpty()) {
            stop()
        }

        hasCommand(message)?.let {
            doWorkInMainThread {
                messageReceived?.invoke(it)
            }
        }
    }

    private fun hasCommand(message: String?): SocketClientState? {
        if (message.isNullOrEmpty()) {
            return null
        }
        var command: BaseCommand = gson.fromJson(message, Command::class.java)
        val socketState: SocketClientState

        when ((command as? Command)?.toModel?.commandType) {
            CommandType.ONLINE -> {
                command = message.fromJson<Online>()
                socketState = SocketClientState.Online(command.toModel)
            }
            CommandType.OFFLINE -> {
                command = message.fromJson<Offline>()
                server = null
                socketState = SocketClientState.Offline(command.toModel)
            }
            CommandType.FILE -> {
                command = message.fromJson<File>()
                socketState = SocketClientState.File(command.toModel)
            }
            else -> {
                processReceivedWithoutUi(command)
                return null
            }
        }

        println(message)

        return socketState
    }

    private fun processReceivedWithoutUi(command: BaseCommand) {
        when ((command as? Command)?.toModel?.commandType) {
            CommandType.REQUEST_PING -> socketWorker.sendResponsePing()
            else -> return
        }
    }

    fun stop() {
        mRun = false
        doWork {
            socketWorker.sendOffline()
            mBufferOut?.let {
                it.flush()
                it.close()
            }
            runCatching {
                socket.takeIf { it != null && it.isConnected }?.close()
            }
            messageReceived = null
            mBufferOut = null
            mBufferIn = null
            mServerMessage = null
            onDisconnected?.invoke()
        }
    }

    fun sendMessage(message: String) {
        mBufferOut.takeIf { it != null && !it.checkError() }?.let {
            runCatching {
                it.println(message)
                it.flush()
            }.onFailure {
                it.message
            }
        }
        doWork {
            mutex.withLock {
                startTime = System.currentTimeMillis()
            }
        }
    }

    fun sendFile2(file: java.io.File) {

        // Get the size of the file
        // Get the size of the file
        val length = file.length()
        val bytes = ByteArray(file.length().toInt())
        val `in`: InputStream = FileInputStream(file)
        val out = socket!!.getOutputStream()

        var count: Int
        while (`in`.read(bytes).also { count = it } > 0) {
            out.write(bytes, 0, count)
        }

        //out.close()
        //`in`.close()
        //socket!!.close()
    }

    fun sendFile3(file: java.io.File) {
        if (socket == null) {
            return
        }
        val mybytearray = ByteArray(file.length().toInt())
        val bis = BufferedInputStream(FileInputStream(file))
        bis.read(mybytearray, 0, mybytearray.size)
        val os: OutputStream = socket!!.getOutputStream()
        os.write(mybytearray, 0, mybytearray.size)
        os.flush()

    }

    fun sendFile(file: java.io.File) {
        val soc = SocketClientSendFile(socketWorker)
        soc.start(address, name, file)

/*        val socFile = FileClientChannel()
        socFile.start(file.path)*/
    }

}