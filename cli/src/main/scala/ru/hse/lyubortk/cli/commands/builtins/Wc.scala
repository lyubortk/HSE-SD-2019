package ru.hse.lyubortk.cli.commands.builtins

import java.io.{FileInputStream, IOException, InputStream}
import java.nio.charset.{CharacterCodingException, MalformedInputException}

import org.apache.commons.io.input.CountingInputStream
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.{Command, CommandResult}

import scala.io.Source
import scala.util.{Failure, Success, Try, Using}

object Wc extends Command {
  // visible for testing
  private[builtins] val CharsetErrorMessage = "Cannot parse input with system-default encoding"

  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult = {
    if (args.isEmpty) {
      processStdin(stdin)
    } else {
      processArguments(args)
    }
  }

  private def processArguments(args: Seq[String]): CommandResult = {
    val errBuilder = new StringBuilder
    val outputBuilder = new StringBuilder
    val totalInfo = args.foldLeft(Info.empty) { case (accumulatedInfo, fileName) =>
      val currentInfo =
        Using(new FileInputStream(fileName)) { inputStream =>
          readInputInfo(inputStream)
        } match {
          case Success(info) =>
            outputBuilder.append(info).append(' ').append(fileName).append("\n")
            info
          case Failure(_: CharacterCodingException) =>
            errBuilder.append(CharsetErrorMessage).append("\n")
            Info.empty
          case Failure(exception) =>
            errBuilder.append(exception.getMessage).append("\n")
            Info.empty
        }
      accumulatedInfo combine currentInfo
    }
    if (args.size > 1) {
      outputBuilder.append(totalInfo).append(' ').append("total").append("\n")
    }
    Continue(outputBuilder.toString().inputStream, errBuilder.toString().inputStream)
  }

  private def processStdin(stdin: InputStream): CommandResult =
    Try(readInputInfo(stdin)) match {
      case Success(value) => Continue(value.toString.inputStream.withNewline)
      case Failure(_: CharacterCodingException) =>
        Continue(InputStream.nullInputStream(), CharsetErrorMessage.inputStream.withNewline)
      case Failure(exception) => Continue(InputStream.nullInputStream(), exception.getMessage.inputStream.withNewline)
    }

  @throws[IOException]
  private def readInputInfo(input: InputStream): Info = {
    val countingInput = new CountingInputStream(input)
    val source = Source.fromInputStream(countingInput)
    val (lines, words) = source.getLines().foldLeft((0, 0)) {
      case ((lines, words), currentLine) =>
        (lines + 1, words + currentLine.split("\\s+").length)
    }
    Info(lines, words, countingInput.getCount)
  }

  protected case class Info(lines: Int, words: Int, bytes: Int) {
    def combine(other: Info): Info = Info(
      lines + other.lines,
      words + other.words,
      bytes + other.bytes
    )

    override val toString: String = s"$lines $words $bytes"
  }

  object Info {
    val empty: Info = Info(0, 0, 0)
  }

}
