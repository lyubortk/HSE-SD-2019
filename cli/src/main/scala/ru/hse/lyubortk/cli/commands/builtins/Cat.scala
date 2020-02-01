package ru.hse.lyubortk.cli.commands.builtins

import java.io.{FileInputStream, InputStream, SequenceInputStream}

import ru.hse.lyubortk.cli.commands.Command
import scala.jdk.CollectionConverters._

object Cat extends Command {
  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): Option[InputStream] = {
    val output = new SequenceInputStream(
      args.iterator.map(new FileInputStream(_)).asJavaEnumeration
    )
    Some(output)
  }
}
