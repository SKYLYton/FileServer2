package socket.client

import model.ServerOnlineModel
import state.SocketClientState
/**
 * @author Fedotov Yakov
 */
interface SocketClientWorker {
    fun startReceivingServerMessages(
        messageReceived: ((SocketClientState) -> Unit),
        onConnected: ((ServerOnlineModel) -> Unit),
        onDisconnected: (() -> Unit),
        clientStartingListener: ((isStart: Boolean, server: ServerOnlineModel?) -> Unit)
    )

    fun stopReceivingMessages()

    fun isClientStart(): Boolean
    fun startClient(address: String, name: String)
    fun stopClient()

    fun sendOnline(name: String)
    fun sendOffline()

    fun sendFile(name: String)

    fun sendResponsePing()
}