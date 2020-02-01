package ru.hse.lyubortk.cli.commands

import java.io.InputStream

import scala.collection.mutable
import scala.sys.process.{BasicIO, Process}

class CommandExecutor(env: mutable.Map[String, String], builtins: Map[String, Command]) {
  import ru.hse.lyubortk.cli.commands.CommandExecutor.getExternalCommand

  def execute(command: String, arguments: Seq[String], stdin: InputStream): Option[InputStream] =
    builtins
      .getOrElse(command, getExternalCommand(command))
      .execute(arguments, stdin, env.toSeq)
}

object CommandExecutor {
  def getExternalCommand(name: String): Command =
    (args: Seq[String], stdin: InputStream, env: Seq[(String, String)]) => {
    var processOutput: InputStream = null

    val io = BasicIO.standard(true)
      .withInput { processStdin =>
        stdin.transferTo(processStdin)
        processStdin.close()
      }
      .withOutput { processStdout =>
        processOutput = processStdout
      }

    Process(name +: args, None, env: _*).run(io)
    Some(processOutput)
  }
}