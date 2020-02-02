package ru.hse.lyubortk.cli.commands.builtins

import java.io.{FileInputStream, InputStream, SequenceInputStream}

import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.{Command, CommandResult}

import scala.jdk.CollectionConverters._

object Cat extends Command {
  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult = {
    val output = new SequenceInputStream(
      args.iterator.map(new FileInputStream(_)).asJavaEnumeration
    )
    Continue(output)
  }
}
