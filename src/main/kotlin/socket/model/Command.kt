package socket.model

import socket.model.BaseCommand


/**
 * @author Fedotov Yakov
 */
class Command : BaseCommand()

enum class CommandType {
    ONLINE, OFFLINE, FILE, RESPONSE_PING, REQUEST_PING
}