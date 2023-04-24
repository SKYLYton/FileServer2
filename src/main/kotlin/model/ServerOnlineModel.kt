package model

import socket.model.BaseCommand
import socket.model.ServerOnline

/**
 * @author Fedotov Yakov
 */
class ServerOnlineModel(
) : BaseCommand("server_online")

val ServerOnline.toModel: ServerOnlineModel
    get() = ServerOnlineModel()