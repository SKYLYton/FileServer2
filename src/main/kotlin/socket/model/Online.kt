package socket.model

import com.google.gson.annotations.SerializedName
import socket.model.BaseCommand

/**
 * @author Fedotov Yakov
 */
data class Online(
    @SerializedName("name")
    val name: String = ""
): BaseCommand("Online")