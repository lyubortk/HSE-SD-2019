package ru.hse.lyubortk.cli.commands.builtins

import java.io.{ByteArrayInputStream, InputStream}

import ru.hse.lyubortk.cli.commands.Command

object Echo extends Command {
  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): Option[InputStream] =
    Some(new ByteArrayInputStream(args.mkString(" ").getBytes))
}
