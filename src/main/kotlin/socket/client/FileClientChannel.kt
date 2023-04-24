package socket.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import socket.BaseSocket
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.SocketChannel


class FileClientChannel : BaseSocket() {
    private var socket: SocketChannel? = null
    private var clientSerial = 0

    private var running = false

    fun start(name: String) {
        doWork {
            try {
                running = true
                withContext(Dispatchers.IO) {
                    startClientOnce(name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        shutdownServer()
    }

    // close server,
    private fun shutdownServer() {
        try {
            running = false
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun startClientOnce(name: String) {
        // start client in a new thread
        try {
            socket = SocketChannel.open(InetSocketAddress("192.168.10.11", 5757))

/*            val fileChannel: FileChannel = FileInputStream(name).channel

            fileChannel.transferTo(0, fileChannel.size(), socket)*/
            sendFile(File(name))

            //fileChannel.close()


/*            // write
            val request = "hello - from client [" + Thread.currentThread().name + "}"
            val bs = request.toByteArray(StandardCharsets.UTF_8)
            val buffer = ByteBuffer.wrap(bs)
            while (buffer.hasRemaining()) {
                socket!!.write(buffer)
            }

            // read
            val inBuf = ByteBuffer.allocate(1024)
            while (socket!!.read(inBuf) > 0) {
                System.out.printf(
                    "[%s]:\t%s\n",
                    Thread.currentThread().name,
                    String(inBuf.array(), StandardCharsets.UTF_8)
                )
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendFile(file:File) {
        val sbc = FileChannel.open(file.toPath())
        val bout = FileInputStream(file)
        //val sbc = bout.channel
        val buff: ByteBuffer = ByteBuffer.allocate(64 * 8192)

        //var bytesread: Int = sbc.read(buff)

        while ((sbc?.read(buff) ?: 0) > 0) {
            buff.flip();
            socket?.write(buff);
            buff.clear();
        }

        val i = 0

        i.toShort()

/*        while (bytesread != -1) {
            buff.flip()
            socket?.write(buff)
            buff.compact()
            bytesread = sbc.read(buff)
        }*/
    }

}