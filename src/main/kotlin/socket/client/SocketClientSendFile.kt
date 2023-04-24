package socket.client

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.ServerOnlineModel
import model.toModel
import socket.BaseSocket
import socket.model.*
import socket.model.File
import state.SocketClientState
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets


/**
 * @author Fedotov Yakov
 */
class SocketClientSendFile constructor(
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

    private var socket: Socket? = null

    fun isStart() = mRun

    fun start(address: String, name: String, file: java.io.File) {
        if (mRun) {
            return
        }
        this.address = address
        if (name.isNotEmpty()) {
            this.name = name
        }
        runClient(file)
    }

    private fun runClient(file: java.io.File) {
        mRun = true
        doWork {
            runCatching {
                socket = Socket(address, 5757).apply {
                    sendBufferSize = 32 * 8192
                    receiveBufferSize = 32 * 8192
                }

                sendFile(file)
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
    }

    fun stop() {
        mRun = false
        doWork {
            socketWorker.sendOffline()
            runCatching {
                socket.takeIf { it != null && it.isConnected }?.close()
            }
            messageReceived = null
            mServerMessage = null
            onDisconnected?.invoke()
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
        while (fileInputStream.read(buffer).also { read = it } != -1) {
            socketOutputStream.write(buffer, 0, read)
            readTotal += read
        }


        //dOut.write(fileBytes, 0, fileBytes.size);

    }

    private fun readFileToBytes(file: java.io.File): ByteArray {
        val bytes = ByteArray(file.length().toInt())
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)

            //read file into bytes[]
            fis.read(bytes)
        } finally {
            fis?.close()
        }
        return bytes
    }

}