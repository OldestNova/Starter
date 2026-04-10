import kotlinx.cinterop.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.posix.*

object Shared {
    @Serializable
    data class Config(
        val groupName: String,
        val directory: String = ".",
        val execute: String,
        val hideConsole: Boolean = false,
        val elevated: Boolean = false,
        val passthroughParent: Boolean = false,
        val extraArgs: Array<String> = emptyArray()
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true

            other as Config

            if (groupName != other.groupName) return false
            if (directory != other.directory) return false
            if (execute != other.execute) return false
            if (hideConsole != other.hideConsole) return false
            if (elevated != other.elevated) return false
            if (passthroughParent != other.passthroughParent) return false
            if (!extraArgs.contentEquals(other.extraArgs)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = groupName.hashCode()
            result = 31 * result + directory.hashCode()
            result = 31 * result + execute.hashCode()
            result = 31 * result + hideConsole.hashCode()
            result = 31 * result + elevated.hashCode()
            result = 31 * result + passthroughParent.hashCode()
            result = 31 * result + extraArgs.contentHashCode()
            return result
        }
    }

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
    }
    fun readConfig(): Config {
        if (access("starter-config.json", F_OK) != 0) {
            val defaultConfig = Config("group-bane", execute = "want-to-execute.exe")
            val defaultConfigString = json.encodeToString(defaultConfig)
            val file = fopen("starter-config.json", "w") ?: throw IllegalArgumentException("Cannot open input file")
            try {
                fputs(defaultConfigString, file)
            } finally {
                fclose(file)
            }
            throw IllegalArgumentException("Config file not found. Default config file created.")
        }

        val returnBuffer = StringBuilder()
        val file = fopen("starter-config.json", "r") ?: throw IllegalArgumentException("Cannot open input file")
        try {
            memScoped {
                val bufferLength = 64
                val buffer = allocArray<ByteVar>(bufferLength)
                while (true) {
                    val nextLine = fgets(buffer, bufferLength, file)?.toKString() ?: break
                    returnBuffer.append(nextLine)
                }
            }
        } finally {
            fclose(file)
        }
        val conf = Json.decodeFromString<Config>(returnBuffer.toString())
        return conf
    }
}