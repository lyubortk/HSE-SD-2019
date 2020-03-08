package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.{Command, CommandResult}

/**
 * Prints its arguments to 'output' stream in CommandResult.
 */
object Echo extends Command {
  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult =
    Continue(args.mkString(" ").inputStream.withNewline)
}
