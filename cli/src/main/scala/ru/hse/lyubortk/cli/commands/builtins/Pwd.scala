package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.{Command, CommandResult}

import scala.util.{Failure, Success, Try}

/**
 * Returns current working directory.
 */
object Pwd extends Command {
  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult = {
    Try(System.getProperty("user.dir")) match {
      case Success(value) => Continue(value.inputStream.withNewline)
      case Failure(exception) => Continue(InputStream.nullInputStream(), exception.getMessage.inputStream.withNewline)
    }
  }
}
