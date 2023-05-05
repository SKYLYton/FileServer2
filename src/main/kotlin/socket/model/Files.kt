package socket.model

import socket.model.BaseCommand

data class Files(val list: List<File>): BaseCommand("Files")