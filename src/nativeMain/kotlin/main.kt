import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        val config = Shared.readConfig()
        makeProcessGroup(config.groupName)
        val (_, status) = runBlocking {
            executeCommandAndCaptureOutput(listOf(config.execute, *args), ExecuteCommandOptions(config.directory, abortOnError = false, redirectStderr = true, trim = false))
        }
        exitProcess(status)
    } catch (e: Throwable) {
        println(e.message)
        exitProcess(1)
    }
}