package state

import model.*
import socket.model.ReadyReceiveFile

/**
 * @author Fedotov Yakov
 */
sealed class SocketServerState {
    data class Error(val throwable: Throwable) : SocketServerState()
    data class Online(val online: OnlineModel) : SocketServerState()
    data class ServerOnline(val online: ServerOnlineModel) : SocketServerState()
    data class Offline(val offline: OfflineModel) : SocketServerState()
    data class File(val file: FileModel) : SocketServerState()
    object OnlineTest : SocketServerState()
    data class ServerStarted(val isStart: Boolean): SocketServerState()
    data class ClientStarted(val isStart: Boolean): SocketServerState()

    data class ReadyReceiveFile(val file: ReadyReceiveFileModel) : SocketServerState()

}
