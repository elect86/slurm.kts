package slurm

import java.io.File
import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.TimeUnit

val sinfo by lazy { "sinfo"() }

fun main() {
    "ls -la"()
}

operator fun String.invoke() {
    ProcessBuilder(*split(" ").toTypedArray())
//        .directory(workingDir)
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT)
        .start()
        .waitFor(60, TimeUnit.MINUTES)
}