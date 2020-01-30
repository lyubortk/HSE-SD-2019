package ru.hse.lyubortk.cli.commands

import java.io.{InputStream, OutputStream}

import scala.sys.process.{Process, ProcessIO}
import scala.util.control.NonFatal

class CommandExecutor {
  def execute(command: String, arguments: Seq[String], stdin: InputStream, env: Seq[(String, String)]): InputStream = {
    var processOutput: InputStream = null
    var processInput: OutputStream = null
    var processErrOutput: InputStream = null

    val inputHandler: OutputStream => Unit = { input =>
      processInput = input
      try {
        stdin.transferTo(processInput)
      } catch {
        case NonFatal(e) => System.err.println(e)
      } finally {
        stdin.close()
      }
    }
    val outputHandler: InputStream => Unit = processOutput = _
    val errOutputHandler: InputStream => Unit = processErrOutput = _
    val processIO: ProcessIO = new ProcessIO(inputHandler, outputHandler, errOutputHandler)
    Process(command +: arguments, None, ("a", "a")).run(processIO).exitValue()
    processErrOutput.transferTo(System.err)

    try {
      processErrOutput.close()
    } catch {
      case NonFatal(e) => System.err.println(e)
    }
    try {
      processInput.close()
    } catch {
      case NonFatal(e) => System.err.println(e)
    }
    processOutput
  }
}
