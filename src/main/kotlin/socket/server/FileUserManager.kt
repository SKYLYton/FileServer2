package socket.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.toModel
import socket.BaseSocket
import socket.model.*
import state.SocketServerState
import java.io.*
import java.io.File
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit


/**
 * @author Fedotov Yakov
 */
class FileUserManager(
    var socket: Socket?
) : BaseSocket() {
    var fileSaved: ((second: Int) -> Unit)? = null
    var savedProcessListener: ((progress: Int) -> Unit)? = null

    var fileReceive: ((isStart: Boolean) -> Unit)? = null

    var messageReceived: ((message: SocketServerState) -> Unit)? = null

    private var bufferInput: BufferedReader? = null

    private var running = false

    private val mutex = Mutex()

    private var startTime = System.currentTimeMillis()
    private var timeWork = System.currentTimeMillis()

    @Volatile
    private var progress: Double = 0.0

    private fun runSocket(file: File) {
        running = true
        doWork {
            // читаем сообщение от клиента
            bufferInput = runCatching {
                BufferedReader(InputStreamReader(socket?.getInputStream(), StandardCharsets.UTF_8))
            }.getOrNull()

            bufferInput?.let {
                var isMessageReceived = false

                while (running) {
                    runCatching {
                        isMessageReceived = processSocket(it)
                    }
                    if (isMessageReceived) {
                        break
                    }
                }
                sendFile(file)
            }
            running = false

            fileReceive?.invoke(false)

            timeWork = System.currentTimeMillis() - startTime
            fileSaved?.invoke(
                TimeUnit.MILLISECONDS.toSeconds(timeWork).toInt()
            )
        }
    }

    private suspend fun processSocket(input: BufferedReader): Boolean {
        val message: String?
        mutex.withLock {
            message = kotlin.runCatching { input.readLine() }.getOrNull()
        }

        fetchCommand(message)?.let {
            messageReceived?.invoke(it)
            if (it is SocketServerState.ReadyReceiveFile) {
                return true
            }
        }
        return false
    }

    private suspend fun fetchCommand(message: String?): SocketServerState? {
        if (message.isNullOrEmpty()) {
            return null
        }
        //Log.e("TAG", "Сообщение: $message")
        var command: BaseCommand = gson.fromJson(message, Command::class.java)

        var socketState: SocketServerState? = null

        if ((command as? Command)?.toModel?.commandType == CommandType.READY_RECEIVE_FILE) {
            command = gson.fromJson(message, ReadyReceiveFile::class.java)
            socketState = SocketServerState.ReadyReceiveFile(command.toModel)
        }

        return socketState
    }

    private fun updateProgress() {
        doWork {
            var oldProgress = progress
            fileSaved?.invoke(0)
            while (running || progress < 100) {
                delay(200)
                if (oldProgress < progress) {
                    savedProcessListener?.invoke(progress.toInt())
                    oldProgress = progress
                }
            }
            fileSaved?.invoke(
                TimeUnit.MILLISECONDS.toSeconds(timeWork).toInt()
            )
        }
    }


    fun sendFile(file: java.io.File) {
        //val fileBytes = readFileToBytes(file)

        //val dOut = BufferedOutputStream(socket!!.getOutputStream(), 32 * 8192)

        val fileInputStream = FileInputStream(file)
        val socketOutputStream = DataOutputStream(socket!!.getOutputStream())
        val startTime = System.currentTimeMillis()
        val buffer = ByteArray(32 * 8192)
        var read: Int
        var readTotal = 0
        fileReceive?.invoke(true)
        while (fileInputStream.read(buffer).also { read = it } != -1) {
            socketOutputStream.write(buffer, 0, read)
            readTotal += read
        }


        //dOut.write(fileBytes, 0, fileBytes.size);

    }


    fun open(file: File) {
        runSocket(file)
    }

    fun close() {
        running = false
        kotlin.runCatching {
            socket?.close()
        }
        socket = null
    }
}