package ru.hse.lyubortk.cli

import java.io.InputStream

import ru.hse.lyubortk.cli.commands.CommandResult.{Continue, Exit}
import ru.hse.lyubortk.cli.commands.{CommandExecutor, CommandResult}
import ru.hse.lyubortk.cli.parsing.ParsingError
import ru.hse.lyubortk.cli.parsing.ast.Expression._
import ru.hse.lyubortk.cli.parsing.ast.{AstParser, Expression}
import ru.hse.lyubortk.cli.parsing.substitution.Token._
import ru.hse.lyubortk.cli.parsing.substitution.{SubstitutionParser, Token}

import scala.collection.mutable

class Cli(env: Map[String, String],
          commandExecutorBuilder: mutable.Map[String, String] => CommandExecutor) {

  private val environment: mutable.Map[String, String] = mutable.Map(env.toSeq: _*).withDefault(_ => "")
  private val commandExecutor = commandExecutorBuilder(environment)

  def start(): Unit = {
    Iterator.continually(io.StdIn.readLine)
      .takeWhile(_ != null)
      .flatMap(parseSubstitutions)
      .map(processSubstitutions)
      .flatMap(parseAst)
      .map(processExpression)
      .tapEach(printResult)
      .takeWhile {
        case _: Exit => false
        case _ => true
      }
      .foreach(_ => ())
  }

  private def parseSubstitutions(text: String): Option[Seq[Token]] = SubstitutionParser(text) match {
    case Left(ParsingError(message, unparsedText)) =>
      System.err.println(message)
      None
    case Right(tokens) =>
      Some(tokens)
  }

  private def processSubstitutions(tokens: Seq[Token]): String = tokens.map {
    case SubstitutionText(text) => environment(text)
    case token => token.text
  }.mkString

  private def parseAst(text: String): Option[Expression] = AstParser(text) match {
    case Left(ParsingError(message, unparsedText)) =>
      System.err.println(message)
      None
    case Right(ast) =>
      Some(ast)
  }

  private def processExpression(expression: Expression): CommandResult = expression match {
    case AssignmentExpression(Word(identifier), Text(argument)) => processAssignment(identifier, argument)
    case PipelineExpression(commands) => processPipeline(commands)
  }

  private def processAssignment(identifier: String, argument: String): CommandResult = {
    environment.put(identifier, argument)
    Continue(InputStream.nullInputStream())
  }

  private def processPipeline(commands: Seq[Command]): CommandResult = {
    commands.foldLeft(Continue(InputStream.nullInputStream): CommandResult) {
      case (Continue(input), Command(commandName, arguments)) =>
        val output = commandExecutor.execute(commandName.text, arguments.map(_.text), input)
        input.close()
        output
      case (exit, _) => exit
    }
  }

  private def printResult(result: CommandResult): Unit = {
    val CommandResult(output) = result
    output.transferTo(System.out)
    println()
    output.close()
  }
}
