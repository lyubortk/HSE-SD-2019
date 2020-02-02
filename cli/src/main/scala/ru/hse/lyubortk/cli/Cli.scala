package ru.hse.lyubortk.cli

import java.io.InputStream

import ru.hse.lyubortk.cli.commands.CommandExecutor
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
      .map {
        case AssignmentExpression(Word(identifier), Text(argument)) => processAssignment(identifier, argument)
        case PipelineExpression(commands) => processPipeline(commands)
      }
      .takeWhile(_.nonEmpty)
      .flatten
      .foreach { pipelineOutput: InputStream =>
        pipelineOutput.transferTo(System.out)
        println()
        pipelineOutput.close()
      }
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

  private def processAssignment(identifier: String, argument: String): Option[InputStream] = {
    environment.put(identifier, argument)
    Some(InputStream.nullInputStream())
  }

  private def processPipeline(commands: Seq[Command]): Option[InputStream] = {
    commands.foldLeft(Option(InputStream.nullInputStream)) {
      case (Some(input), Command(commandName, arguments)) =>
          val output = commandExecutor.execute(commandName.text, arguments.map(_.text), input)
          input.close()
          output
      case (None, _) => None
    }
  }
}
