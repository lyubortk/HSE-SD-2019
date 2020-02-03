package ru.hse.lyubortk.cli.commands.builtins

import java.nio.charset.Charset

import org.apache.commons.io.IOUtils
import ru.hse.lyubortk.cli.commands.CommandResult

object Utils {
  protected[builtins] def extractOutput(result: CommandResult): (String, String) = {
    val CommandResult(output, errOutput) = result
    (IOUtils.toString(output, Charset.defaultCharset()), IOUtils.toString(errOutput, Charset.defaultCharset()))
  }
}
