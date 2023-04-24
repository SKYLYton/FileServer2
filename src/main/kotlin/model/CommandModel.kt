package model

import socket.model.Command
import socket.model.CommandType

/**
 * @author Fedotov Yakov
 */
data class CommandModel(
    val command: String = "",
    val commandType: CommandType
)

val Command.toModel: CommandModel
    get() = CommandModel(
        command,
        CommandType.valueOf(command.uppercase())
    )