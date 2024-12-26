expect suspend fun executeCommandAndCaptureOutput(
    command: List<String>,
    options: ExecuteCommandOptions
): Pair<String, Int>

data class ExecuteCommandOptions(
    val directory: String,
    val abortOnError: Boolean,
    val redirectStderr: Boolean,
    val trim: Boolean
)

expect fun makeProcessGroup(name: String): Unit

// call $ which $executable on the JVM
expect suspend fun findExecutable(executable: String): String

expect fun hideConsole(): Unit