package ru.hse.lyubortk.cli.commands.builtins

import java.io.{FileInputStream, InputStream, SequenceInputStream}
import java.nio.charset.Charset

import org.apache.commons.io.IOUtils
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.{Command, CommandResult}

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

/**
 * Returns contents of files specified as arguments. Reads specified stdin stream if no arguments are given.
 * Returns OS-specific error in 'errOutput' for files that cannot be opened (valid files will still be processed).
 */
object Cat extends Command {
  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult = {
    if (args.isEmpty) {
      processStdin(stdin)
    } else {
      processArguments(args)
    }
  }

  private def processArguments(args: Seq[String]): CommandResult = {
    val errBuilder = new StringBuilder
    val fileInputStreams: Seq[InputStream] = args.map {
      name => Try(new FileInputStream(name).withNewline)
    }.tapEach {
      case Failure(exception) => errBuilder.append(exception.getMessage).append("\n")
      case _ => ()
    }.flatMap {
      case Failure(_) => None
      case Success(value) => Option(value)
    }

    val output = new SequenceInputStream(
      fileInputStreams.iterator.asJavaEnumeration
    )
    Continue(output, errBuilder.toString().inputStream)
  }

  private def processStdin(stdin: InputStream): CommandResult = {
    Continue(IOUtils.toString(stdin, Charset.defaultCharset()).inputStream)
  }
}
