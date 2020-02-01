package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import ru.hse.lyubortk.cli.commands.Command

object Exit extends Command {
  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): Option[InputStream] = None
}
