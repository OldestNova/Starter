import kotlinx.cinterop.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.posix.*

object Shared {
    @Serializable
    data class Config(val groupName: String, val directory: String = ".", val execute: String, val hideConsole: Boolean = false, val elevated: Boolean = false)
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