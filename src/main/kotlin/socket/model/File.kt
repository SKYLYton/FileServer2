package socket.model

import socket.model.BaseCommand

data class File(val data: String, val size: Int): BaseCommand("File")