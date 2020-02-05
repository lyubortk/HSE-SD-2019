package ru.hse.lyubortk.cli.commands

import java.io.InputStream

import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._

import scala.sys.process.{BasicIO, Process}
import scala.util.Using
import scala.util.control.NonFatal

/**
 * This class is aimed at resolving internal commands by their name and executing them. Will try to execute
 * external command in a separate process if the command name can't be resolved internally.
 *
 * @param builtins internal commands.
 */
class CommandExecutor(builtins: Map[String, Command]) {

  import ru.hse.lyubortk.cli.commands.CommandExecutor.getExternalCommand

  /**
   * Tries to resolve a builtin and execute it. Calls external commands with [[scala.sys.process.Process]] if the
   * specified name is unknown to the executor.
   *
   * @param command   the name of the command
   * @param arguments command arguments
   * @param stdin     this stream will be connected to the stdin of the called command/process
   * @param env       current environment
   */
  def execute(command: String,
              arguments: Seq[String] = Seq.empty,
              stdin: InputStream = InputStream.nullInputStream(),
              env: Seq[(String, String)] = Seq.empty): CommandResult =
    builtins
      .getOrElse(command, getExternalCommand(command))
      .execute(arguments, stdin, env)
}

object CommandExecutor {
  private[commands] def getExternalCommand(name: String): Command = new Command {
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