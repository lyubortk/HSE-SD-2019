package ru.hse.lyubortk.cli.commands

import java.nio.charset.Charset

import org.apache.commons.io.IOUtils

object Utils {
  protected[commands] def extractOutput(result: CommandResult): (String, String) = {
    val CommandResult(output, errOutput) = result
    (IOUtils.toString(output, Charset.defaultCharset()), IOUtils.toString(errOutput, Charset.defaultCharset()))
  }
}
