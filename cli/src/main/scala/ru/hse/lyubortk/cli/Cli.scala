package ru.hse.lyubortk.cli

import java.io.{BufferedReader, InputStream, PrintStream}

import ru.hse.lyubortk.cli.Cli.IO
import ru.hse.lyubortk.cli.Cli._
import ru.hse.lyubortk.cli.commands.CommandResult.{Continue, Exit}
import ru.hse.lyubortk.cli.commands.{CommandExecutor, CommandResult}
import ru.hse.lyubortk.cli.parsing.{CliParser, ParsingError}
import ru.hse.lyubortk.cli.parsing.ast.Expression._
import ru.hse.lyubortk.cli.parsing.ast.Expression
import ru.hse.lyubortk.cli.parsing.substitution.Token
import ru.hse.lyubortk.cli.parsing.substitution.Token._

import scala.collection.mutable
import scala.util.{Failure, Try, Using}

class Cli(env: Map[String, String],
          commandExecutorBuilder: mutable.Map[String, String] => CommandExecutor,
          substitutionParser: CliParser[Seq[Token]],
          astParser: CliParser[Expression],
          io: IO = IO()) {

  import io._
  private implicit val errorImplicit: PrintStream = io.err
  private val environment: mutable.Map[String, String] = mutable.Map(env.toSeq: _*).withDefault(_ => "")
  private val commandExecutor = commandExecutorBuilder(environment)

  def start(): Unit = {
    Iterator.continually(in.readLine)
      .takeWhile(_ != null)
      .flatMap(parse(substitutionParser))
      .map(processSubstitutions)
      .flatMap(parse(astParser))
      .map(processExpression)
      .tapEach(printResult)
      .takeWhile {
        case _: Exit => false
        case _ => true
      }
      .foreach(_ => ())
  }

  private def parse[T](parser: CliParser[T])(text: String) = parser(text) match {
    case Left(ParsingError(message, _)) =>
      err.println(s"Can't parse command. Parser output: $message")
      None
    case Right(result) =>
      Some(result)
  }

  private def processSubstitutions(tokens: Seq[Token]): String = tokens.map {
    case SubstitutionText(text) => environment(text)
    case token => token.text
  }.mkString

  private def processExpression(expression: Expression): CommandResult = expression match {
    case AssignmentExpression(Word(identifier), Text(argument)) => processAssignment(identifier, argument)
    case PipelineExpression(commands) => processPipeline(commands)
  }

  private def processAssignment(identifier: String, argument: String): CommandResult = {
    environment.put(identifier, argument)
    Continue()
  }

  private def processPipeline(commands: Seq[Command]): CommandResult = {
    commands.foldLeft(Continue(InputStream.nullInputStream): CommandResult) {
      case (Continue(output, errOutput), Command(commandName, arguments)) =>
        val result = commandExecutor.execute(commandName.text, arguments.map(_.text), output)
        Try(output.close()).printError
        Using(errOutput)(_.transferTo(err)).printError
        result
      case (exit, _) => exit
    }
  }

  private def printResult(result: CommandResult): Unit = {
    val CommandResult(output, errOutput) = result
    Using(output)(_.transferTo(out)).printError
    Using(errOutput)(_.transferTo(err)).printError
  }
}

object Cli {
  case class IO(out: PrintStream = Console.out,
                err: PrintStream = Console.err,
                in: BufferedReader = Console.in)

  implicit class TryErrPrinter(val result: Try[_]) extends AnyVal {
    def printError(implicit err: PrintStream): Unit = {
      result match {
        case Failure(exception) => err.println(s"An error occurred while executing commands: ${exception.getMessage}")
        case _ => ()
      }
    }
  }
}
