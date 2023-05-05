package model

import socket.model.BaseCommand
import socket.model.ReadyReceiveFile


/**
 * @author Fedotov Yakov
 */
class ReadyReceiveFileModel(): BaseCommand("ready_receive_file")

val ReadyReceiveFile.toModel: ReadyReceiveFileModel
    get() = ReadyReceiveFileModel()

