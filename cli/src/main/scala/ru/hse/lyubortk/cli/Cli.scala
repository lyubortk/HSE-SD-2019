package ru.hse.lyubortk.cli

import java.io.{BufferedReader, InputStream, PrintStream}

import ru.hse.lyubortk.cli.Cli.{IO, _}
import ru.hse.lyubortk.cli.commands.CommandResult.{Continue, Exit}
import ru.hse.lyubortk.cli.commands.{CommandExecutor, CommandResult}
import ru.hse.lyubortk.cli.parsing.ast.Expression
import ru.hse.lyubortk.cli.parsing.ast.Expression._
import ru.hse.lyubortk.cli.parsing.substitution.Token
import ru.hse.lyubortk.cli.parsing.substitution.Token._
import ru.hse.lyubortk.cli.parsing.{CliParser, ParsingError}

import scala.collection.mutable
import scala.util.{Failure, Try, Using}

/**
 * A simple bash-like command line interpreter. Reads input line by line, parses it with provided parsers and then
 * executes commands with CommandExecutor. Exits when the end of input is reached or some command tells interpreter to
 * stop.
 *
 * @param initialEnvironment interpreter environment will be initialized with values from this map
 * @param commandExecutor    responsible for processing command calls
 * @param substitutionParser parses substitutions from raw text
 * @param astParser          parses expressions from text with resolved substitutions
 * @param io                 optional parameter with I/O settings (stdout, stderr and stdin by default)
 */
class Cli(initialEnvironment: Map[String, String],
          commandExecutor: CommandExecutor,
          substitutionParser: CliParser[Seq[Token]],
          astParser: CliParser[Expression],
          io: IO = IO()) {

  import io._

  private implicit val errorImplicit: PrintStream = io.err
  private val env: mutable.Map[String, String] = mutable.Map(initialEnvironment.toSeq: _*).withDefault(_ => "")

  /**
   * Runs interpreter loop in the caller thread.
   */
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
    case SubstitutionText(text) => env(text)
    case token => token.text
  }.mkString

  private def processExpression(expression: Expression): CommandResult = expression match {
    case AssignmentExpression(Word(identifier), Text(argument)) => processAssignment(identifier, argument)
    case PipelineExpression(commands) => processPipeline(commands)
  }

  private def processAssignment(identifier: String, argument: String): CommandResult = {
    env.put(identifier, argument)
    Continue()
  }

  private def processPipeline(commands: Seq[Command]): CommandResult = {
    commands.foldLeft(Continue(InputStream.nullInputStream): CommandResult) {
      case (Continue(output, errOutput), Command(commandName, arguments)) =>
        val result = commandExecutor.execute(commandName.text, arguments.map(_.text), output, env.toSeq)
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

  /**
   * This class is used to specify I/O settings of Cli.
   *
   * @param out regular output will be written to this stream
   * @param err error output will be written to this stream
   * @param in  Cli will read its input from this stream
   */
  case class IO(out: PrintStream = Console.out,
                err: PrintStream = Console.err,
                in: BufferedReader = Console.in)

  private[cli] implicit class TryErrPrinter(val result: Try[_]) extends AnyVal {
    def printError(implicit err: PrintStream): Unit = {
      result match {
        case Failure(exception) => err.println(s"An error occurred while executing commands: ${exception.getMessage}")
        case _ => ()
      }
    }
  }

}
