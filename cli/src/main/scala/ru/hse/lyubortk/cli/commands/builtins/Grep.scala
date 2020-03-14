package ru.hse.lyubortk.cli.commands.builtins

import java.io.{IOException, InputStream}

import org.rogach.scallop._
import org.rogach.scallop.exceptions.{Help, ScallopException, Version}
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.builtins.InputProcessingCommand.BaseOptions

import scala.language.postfixOps

import scala.io.Source

/**
 * Searches the named input files for lines containing a match to the given regex.
 * If no files are specified searches in 'stdin' stream. Prints matched lines to 'output' stream.
 */
object Grep extends InputProcessingCommand {
  override protected type ProcessingResult = String
  override protected type OptionsType = GrepOptions

  override protected def parseArguments(args: Seq[String]): GrepOptions = new GrepOptions(args)

  @throws[IOException]
  override protected def processInput(input: InputStream, options: OptionsType): String = {
    val source = Source.fromInputStream(input)
    var patternString = options.pattern()
    if (options.word.getOrElse(false)) {
      patternString = "\\b" + patternString + "\\b"
    }
    if (options.ignoreCase.getOrElse(false)) {
      patternString = "(?i)" + patternString
    }
    val pattern = patternString.r.unanchored
    val after = options.after.getOrElse(0)
    val builder = new StringBuilder
    source.getLines().foldLeft(0) {
      case (_, string) if pattern.matches(string) =>
        builder.append(string).append("\n")
        after
      case (0, _) => 0
      case (left, string) =>
        builder.append(string).append("\n")
        left - 1
    }
    builder.dropRight(1).toString()
  }

  protected class GrepOptions(arguments: Seq[String]) extends ScallopConf(arguments) with BaseOptions {
    private val Header = "Usage: grep [OPTIONS] PATTERN [FILE...]\n"
    val pattern = trailArg[String]("PATTERN", descr = "regex pattern")
    val ignoreCase = toggle("ignore-case", descrYes = "ignore case distinctions")
    val word = toggle("word", descrYes = "force PATTERN to match only whole words")
    val after = opt[Int](
      name = "after-context",
      short = 'A',
      descr = "print NUM lines of trailing context",
      validate = (0 <=)
    )
    private val files = trailArg[List[String]](
      name = "FILE...",
      descr = "Search for PATTERN in each FILE",
      required = false
    )

    var shouldExit = false
    var output: InputStream = InputStream.nullInputStream()
    var errOutput: InputStream = InputStream.nullInputStream()
    def fileNames: Seq[String] = files.getOrElse(List.empty)

    verify()

    override def onError(e: Throwable): Unit = e match {
      case _: Help | Version =>
        output = (Header + builder.help).inputStream.withNewline
        shouldExit = true
      case ScallopException(message) =>

        errOutput = message.inputStream.withNewline
        shouldExit = true
    }
  }
}
