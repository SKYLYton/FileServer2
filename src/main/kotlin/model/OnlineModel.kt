package model

import socket.model.BaseCommand
import socket.model.Online

/**
 * @author Fedotov Yakov
 */
data class OnlineModel(
    val name: String = ""
): BaseCommand("Online")

val Online.toModel: OnlineModel
    get() = OnlineModel(
        name
    )
