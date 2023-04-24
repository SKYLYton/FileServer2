package socket.model

import socket.GsonManager

/**
 * @author Fedotov Yakov
 */
class ServerOnline : BaseCommand("Server_online")

val ServerOnline.toJson: String
    get() = GsonManager.gson.toJson(ServerOnline())