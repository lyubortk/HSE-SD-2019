package ru.hse.lyubortk.cli

import java.io.InputStream

import ru.hse.lyubortk.cli.commands.CommandExecutor
import ru.hse.lyubortk.cli.parsing.PostParser.{AssignmentExpression, Command, PipelineExpression}
import ru.hse.lyubortk.cli.parsing.{PostParser, PreParser}
import ru.hse.lyubortk.cli.parsing.PreParser.SubstitutionText

import scala.collection.mutable

object Cli {

  val context: mutable.Map[String, String] = sys.env.to(mutable.Map).withDefault(_ => "")

  def main(args: Array[String]): Unit = {
    Iterator.continually(io.StdIn.readLine)
      .takeWhile(_ != null)
      .map(PreParser(_))
      .flatMap {
          case Left(exception) =>
            System.err.println(exception)
            None
          case Right(tokens) =>
            Some(tokens)
      }
      .map { tokens =>
        tokens.map {
          case SubstitutionText(text) => context(text)
          case token => token.text
        }.mkString
      }
      .map(PostParser(_))
      .flatMap {
        case Left(exception) =>
          System.err.println(exception)
          None
        case Right(ast) =>
          Some(ast)
      }
      .foreach {
        case AssignmentExpression(identifier, argument) => context.put(identifier.text, argument.text)
        case PipelineExpression(commands) => commands.foldLeft(InputStream.nullInputStream) {
          case (input, Command(commandName, arguments)) =>
            val kek = new CommandExecutor
            kek.execute(commandName.text, arguments.map(_.text), input, context.toSeq)
        }
      }
  }
}
