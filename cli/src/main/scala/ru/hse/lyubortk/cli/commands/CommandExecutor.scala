package ru.hse.lyubortk.cli.commands

import java.io.InputStream

import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._

import scala.collection.mutable
import scala.sys.process.{BasicIO, Process}
import scala.util.Using
import scala.util.control.NonFatal

class CommandExecutor(env: mutable.Map[String, String], builtins: Map[String, Command]) {
  import ru.hse.lyubortk.cli.commands.CommandExecutor.getExternalCommand

  def execute(command: String, arguments: Seq[String], stdin: InputStream): CommandResult =
    builtins
      .getOrElse(command, getExternalCommand(command))
      .execute(arguments, stdin, env.toSeq)
}

object CommandExecutor {
  def getExternalCommand(name: String): Command = new Command {
    var processOutput: InputStream = _
    var processErrOutput: InputStream = _

    override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult = {
      val io = BasicIO.standard(true)
        .withInput { processStdin =>
          Using(processStdin)(stdin.transferTo)
        }
        .withOutput { processStdout =>
          processOutput = processStdout
        }
        .withError { processStderr =>
          processErrOutput = processStderr
        }

      try {
        Process(name +: args, None, env: _*).run(io).exitValue()
        Continue(processOutput, processErrOutput)
      } catch {
        case NonFatal(e) => Continue(InputStream.nullInputStream(), e.getMessage.inputStream.withNewline)
      }
    }
  }
}