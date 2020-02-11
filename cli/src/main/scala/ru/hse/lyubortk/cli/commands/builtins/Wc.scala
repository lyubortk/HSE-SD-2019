package ru.hse.lyubortk.cli.commands.builtins

import java.io.{FileInputStream, IOException, InputStream}
import java.nio.charset.CharacterCodingException

import org.apache.commons.io.input.CountingInputStream
import ru.hse.lyubortk.cli.commands.CommandResult
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.builtins.InputProcessingCommand.BaseOptions

import scala.io.Source
import scala.util.{Failure, Success, Using}

/**
 * Prints (to 'output' stream in CommandResult) line, word, and byte counts for each file specified as arguments.
 * Prints errors to errOutput stream if some files cannot be opened (valid files will still be processed correctly).
 * Reads specified stdin stream if no arguments are given.
 */
object Wc extends InputProcessingCommand {
  type ProcessingResult = Info
  type OptionsType = BaseOptions

  override protected def parseArguments(args: Seq[String]): BaseOptions = new BaseOptions {
    override def fileNames: Seq[String] = args
    override def shouldExit: Boolean = false
    override def output: InputStream = InputStream.nullInputStream()
    override def errOutput: InputStream = InputStream.nullInputStream()
  }

  override def executeWithArguments(config: BaseOptions): CommandResult = {
    val fileNames = config.fileNames
    val errBuilder = new StringBuilder
    val outputBuilder = new StringBuilder

    val totalInfo = fileNames.foldLeft(Info.empty) { case (accumulatedInfo, fileName) =>
      val currentInfo = Using(new FileInputStream(fileName)) { inputStream =>
        processInput(inputStream, config)
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
    if (fileNames.size > 1) {
      outputBuilder.append(totalInfo).append(' ').append("total").append("\n")
    }
    Continue(outputBuilder.toString().inputStream, errBuilder.toString().inputStream)
  }

  @throws[IOException]
  override def processInput(input: InputStream, options: BaseOptions): Info = {
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
