package model

import socket.model.BaseCommand
import socket.model.File

/**
 * @author Fedotov Yakov
 */
data class FileModel(
    val data: String = "",
    val size: Int = 0
): BaseCommand("File")

val File.toModel: FileModel
    get() = FileModel(
        data,
        size
    )
