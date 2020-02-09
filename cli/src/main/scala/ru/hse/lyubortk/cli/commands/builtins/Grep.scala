package ru.hse.lyubortk.cli.commands.builtins

import java.io.{ByteArrayOutputStream, IOException, InputStream}

import ru.hse.lyubortk.cli.commands.builtins.InputProcessingCommand.BaseConfig
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import org.rogach.scallop._
import org.rogach.scallop.exceptions.{Help, ScallopException, Version}

import scala.io.Source

/**
 *
 */
object Grep extends InputProcessingCommand {
  override protected type ProcessingResult = String
  override protected type ConfigType = Conf

  override protected def parseArguments(args: Seq[String]): Conf = new Conf(args)

  @throws[IOException]
  override protected def processInput(input: InputStream, config: ConfigType): String = {
    val source = Source.fromInputStream(input)
    var patternString = config.pattern()
    if (config.word.getOrElse(false)) {
      patternString = "\\b" + patternString + "\\b"
    }
    if (config.ignoreCase.getOrElse(false)) {
      patternString = "(?i)" + patternString
    }
    val pattern = patternString.r.unanchored
    val after = config.after.getOrElse(0)
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

  protected class Conf(arguments: Seq[String]) extends ScallopConf(arguments) with BaseConfig {
    private val Header = "Usage: grep [OPTIONS] PATTERN [FILE...]\n"
    val pattern = trailArg[String]("PATTERN", descr = "regex pattern")
    val ignoreCase = toggle("ignore-case", descrYes = "ignore case distinctions")
    val word = toggle("word", descrYes = "force PATTERN to match only whole words")
    val after = opt[Int]("after-context", short = 'A', descr = "print NUM lines of trailing context")
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
