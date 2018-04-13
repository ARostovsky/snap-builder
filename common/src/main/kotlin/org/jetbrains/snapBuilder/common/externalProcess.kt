package org.jetbrains.snapBuilder.common

import org.slf4j.Logger
import java.util.*

/**
 * @author nik
 */
fun ProcessBuilder.run(log: Logger, verbose: Boolean = false): ProcessOutput {
    val info: (String) -> Unit = if (verbose) { s ->  log.info(s) } else { s -> log.debug(s) }
    info("Executing '${command().joinToString(" ")}'")
    val process = start()
    val output = ArrayList<String>()
    process.inputStream.bufferedReader().forEachLine {
        output.add(it)
        info("[out] $it")
    }
    val errors = ArrayList<String>()
    process.errorStream.bufferedReader().forEachLine {
        errors.add(it)
        info("[err] $it")
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        log.error("Process finished with exit code $exitCode: ${errors.joinToString("\n")}")
    } else {
        info("Process finished successfully")
    }
    return ProcessOutput(exitCode, output)
}

class ProcessOutput(exitCode: Int, val output: List<String>) {

    val isSuccessful = exitCode == 0
    val isFailed = exitCode != 0
}
