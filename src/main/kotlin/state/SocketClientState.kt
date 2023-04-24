package state

import model.FileModel
import model.OfflineModel
import model.OnlineModel
import model.ServerOnlineModel
/**
 * @author Fedotov Yakov
 */
sealed class SocketClientState {
    data class Error(val throwable: Throwable) : SocketClientState()
    data class Online(val online: OnlineModel) : SocketClientState()
    data class Offline(val offline: OfflineModel) : SocketClientState()
    data class File(val song: FileModel) : SocketClientState()
    object OnlineTest : SocketClientState()
    data class ClientStarted(val isStart: Boolean): SocketClientState()
    data class ClientConnected(val online: ServerOnlineModel? = null): SocketClientState()

}