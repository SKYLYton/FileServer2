package model

import socket.model.BaseCommand
import socket.model.File

/**
 * @author Fedotov Yakov
 */
data class FilesModel(
    val list: List<FileModel>
): BaseCommand("Files")

