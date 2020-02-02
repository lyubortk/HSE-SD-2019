package ru.hse.lyubortk.cli.commands.builtins

import java.io.{ByteArrayInputStream, InputStream}

import ru.hse.lyubortk.cli.commands.{Command, CommandResult}
import ru.hse.lyubortk.cli.commands.CommandResult.Continue

object Pwd extends Command {
  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult =
    Continue(new ByteArrayInputStream(System.getProperty("user.dir").getBytes))
}
