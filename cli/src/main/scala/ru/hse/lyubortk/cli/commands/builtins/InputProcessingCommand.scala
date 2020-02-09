package ru.hse.lyubortk.cli.commands.builtins

import java.io.{FileInputStream, IOException, InputStream}
import java.nio.charset.CharacterCodingException

import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.builtins.InputProcessingCommand.BaseConfig
import ru.hse.lyubortk.cli.commands.{Command, CommandResult}

import scala.util.{Failure, Success, Try, Using}

/**
 *
 */
protected trait InputProcessingCommand extends Command {
  protected type ProcessingResult
  protected type ConfigType <: BaseConfig
  protected[builtins] val CharsetErrorMessage = "Cannot parse input with system-default encoding"

  override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult = {
    val options = parseArguments(args)
    if (options.shouldExit) {
      Continue(options.output, options.errOutput)
    } else if (options.fileNames.isEmpty) {
      executeWithStdin(stdin, options)
    } else {
      executeWithArguments(options)
    }
  }

  protected def executeWithArguments(config: ConfigType): CommandResult = {
    val errBuilder = new StringBuilder
    val outputBuilder = new StringBuilder
    config.fileNames.foreach { fileName =>
      Using(new FileInputStream(fileName)) { inputStream =>
        processInput(inputStream, config)
      } match {
        case Success(result) =>
          outputBuilder.append(result).append("\n")
        case Failure(_: CharacterCodingException) =>
          errBuilder.append(CharsetErrorMessage).append("\n")
        case Failure(exception) =>
          errBuilder.append(exception.getMessage).append("\n")
      }
    }
    Continue(outputBuilder.toString().inputStream, errBuilder.toString().inputStream)
  }

  protected def executeWithStdin(stdin: InputStream, config: ConfigType): CommandResult =
    Try(processInput(stdin, config)) match {
      case Success(value) => Continue(value.toString.inputStream.withNewline)
      case Failure(_: CharacterCodingException) =>
        Continue(InputStream.nullInputStream(), CharsetErrorMessage.inputStream.withNewline)
      case Failure(exception) => Continue(InputStream.nullInputStream(), exception.getMessage.inputStream.withNewline)
    }

  @throws[IOException]
  protected def processInput(input: InputStream, config: ConfigType): ProcessingResult

  protected def parseArguments(args: Seq[String]): ConfigType
}

object InputProcessingCommand {
  trait BaseConfig {
    def fileNames: Seq[String]
    def shouldExit: Boolean
    def output: InputStream
    def errOutput: InputStream
  }
}
