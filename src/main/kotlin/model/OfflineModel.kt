package model

import socket.model.BaseCommand
import socket.model.Offline

/**
 * @author Fedotov Yakov
 */
class OfflineModel(): BaseCommand("Offline")

val Offline.toModel: OfflineModel
    get() = OfflineModel()
