package ru.hse.lyubortk.cli.commands.builtins

import java.io.{ByteArrayInputStream, FileInputStream, InputStream}

import org.apache.commons.io.input.CountingInputStream
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.{Command, CommandResult}

import scala.io.Source

object Wc extends Command {
  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult = {
    val output = if (args.isEmpty) {
      processStdin(stdin)
    } else {
      processArguments(args)
    }
    Continue(output)
  }

  private def processArguments(args: Seq[String]): InputStream = {
    val (builder: StringBuilder, totalInfo: Info) =
      args.foldLeft((new StringBuilder, Info(0, 0, 0))) {
        case ((stringBuilder, accumulatedInfo), fileName) =>
          val inputStream = new FileInputStream(fileName)
          val currentInfo = readInputInfo(inputStream)
          inputStream.close()
          stringBuilder.append(currentInfo).append(' ').append(fileName).append("\n")
          (stringBuilder, accumulatedInfo combine currentInfo)
      }
    builder.deleteCharAt(builder.length() - 1)
    if (args.size > 1) {
      builder.append("\n").append(totalInfo).append(' ').append("total")
    }
    new ByteArrayInputStream(builder.toString().getBytes)
  }

  private def processStdin(stdIn: InputStream): InputStream =
    new ByteArrayInputStream(readInputInfo(stdIn).toString.getBytes)

  private def readInputInfo(input: InputStream): Info = {
    val countingInput = new CountingInputStream(input)
    val source = Source.fromInputStream(countingInput)
    val (lines, words) = source.getLines().foldLeft((0, 0)) {
      case ((lines, words), currentLine) =>
        (lines + 1, words + currentLine.split("\\s+").length)
    }
    Info(lines, words, countingInput.getCount)
  }

  private case class Info(lines: Int, words: Int, bytes: Int) {
    def combine(other: Info): Info = Info(
      lines + other.lines,
      words + other.words,
      bytes + other.bytes
    )

    override val toString: String = s"$lines $words $bytes"
  }
}
