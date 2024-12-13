import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.*

actual suspend fun findExecutable(executable: String): String =
    executable

/**
 * https://stackoverflow.com/questions/57123836/kotlin-native-execute-command-and-get-the-output
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun executeCommandAndCaptureOutput(
    command: List<String>,
    options: ExecuteCommandOptions
): Pair<String, Int> {
    chdir(options.directory)
    val commandToExecute = command.joinToString(separator = " ") { arg ->
        if (arg.contains(" ") || arg.contains("%")) "\"$arg\"" else arg
    }
    println("executing: $commandToExecute")
    val redirect = if (options.redirectStderr) " 2>&1 " else ""
    val fp = _popen("$commandToExecute $redirect", "r") ?: error("Failed to run command: $command")

    val stdout = buildString {
        val buffer = ByteArray(4096)
        while (true) {
            val input = fgets(buffer.refTo(0), buffer.size, fp) ?: break
            println(input.toKString())
        }
    }

    val status = _pclose(fp)
    if (status != 0 && options.abortOnError) {
        println(stdout)
        println("failed to run: $commandToExecute")
        throw Exception("Command `$command` failed with status $status${if (options.redirectStderr) ": $stdout" else ""}")
    }

    return Pair(if (options.trim) stdout.trim() else stdout, status)
}

actual fun makeProcessGroup(name: String): Unit {
    val hJob = CreateJobObjectW(null, "$name-job")
    if (hJob == null) {
        println("Failed to create Job Object. Error: ${GetLastError()}")
        return
    }
    memScoped {
        val basicLimitInfo = alloc<JOBOBJECT_BASIC_LIMIT_INFORMATION>().apply {
            LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE.toUInt()
        }

        val extendedLimitInfo = alloc<JOBOBJECT_EXTENDED_LIMIT_INFORMATION>().apply {
            memcpy(
                this.BasicLimitInformation.ptr,
                basicLimitInfo.ptr,
                sizeOf<JOBOBJECT_BASIC_LIMIT_INFORMATION>().toULong()
            )
        }

        val success = SetInformationJobObject(
            hJob,
            JobObjectExtendedLimitInformation,
            extendedLimitInfo.ptr,
            sizeOf<JOBOBJECT_EXTENDED_LIMIT_INFORMATION>().toUInt()
        )

        if (success != TRUE) {
            println("Failed to set Job Object information. Error: ${GetLastError()}")
            CloseHandle(hJob)
            return
        }

        val currentProcess = GetCurrentProcess()
        if (AssignProcessToJobObject(hJob, currentProcess) != TRUE) {
            println("Failed to assign process to Job Object. Error: ${GetLastError()}")
            CloseHandle(hJob)
            return
        }
        println("Current process successfully assigned to Job Object")
    }
}