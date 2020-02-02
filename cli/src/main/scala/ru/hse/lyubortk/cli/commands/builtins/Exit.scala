package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import ru.hse.lyubortk.cli.commands.{Command, CommandResult}

object Exit extends Command {
  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult =
    CommandResult.Exit()
}
