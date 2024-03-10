package bms.ocr.commands

import java.io.File

object CommandRunner {

    data class CommandRunResult(val stdout: String, val stderr: String, val workingDir: File?)
        fun runCommand(name: String,
        index: Int,
        executable: String,
        arguments: Array<String>,
        workingDir: File? = File("work/$index-$executable-out"),
        printStdout: Boolean = true,
        printStderr: Boolean = true
    ): CommandRunResult {
        println()
        println("$name: >>> $index. Running: $executable")

        if (workingDir?.mkdirs() == true) println("Created: \"${workingDir.absolutePath}\"")

        val output = runCatching {
            val proc = ProcessBuilder(executable, *arguments)
                //.directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            //proc.waitFor(20, TimeUnit.SECONDS)
            CommandRunResult(proc.inputReader(Charsets.UTF_8).readText(), proc.errorReader(Charsets.UTF_8).readText(), workingDir).also {
                if (it.stderr.isNotEmpty() && printStderr)
                    println("Error running $executable: ${it.stderr}")
                if (it.stdout.isNotEmpty() && printStdout)
                    println("Done for $executable: ${it.stdout}")
            }
        }.onFailure {
            println("Command runner error:")
            it.printStackTrace()
            println("See stacktrace above.")
        }.getOrThrow()

        return output
    }
}
