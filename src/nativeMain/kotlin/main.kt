import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        val config = Shared.readConfig()
        if (config.passthroughParent) {
            val parentProcessName = getParentProcessName()
            if (!parentProcessName.isNullOrBlank()) {
                setOverrideParentApp(parentProcessName)
            }
        }
        makeProcessGroup(config.groupName)
        if (config.hideConsole) {
            hideConsole()
        }
        val (_, status) = runBlocking {
            if (config.elevated && !isElevated()) {
                elevateSelfAndRun(args)
                Pair("", 0) // Placeholder, as elevateSelfAndRun does not return a value
            } else {
                executeCommandAndCaptureOutput(listOf(config.execute, *config.extraArgs, *args), ExecuteCommandOptions(config.directory, abortOnError = false, redirectStderr = true, trim = false))
            }
        }
        exitProcess(status)
    } catch (e: Throwable) {
        println(e.message)
        exitProcess(1)
    }
}