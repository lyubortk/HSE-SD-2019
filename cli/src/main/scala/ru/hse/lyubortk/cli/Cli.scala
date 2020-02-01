package ru.hse.lyubortk.cli

import java.io.InputStream

import ru.hse.lyubortk.cli.commands.CommandExecutor
import ru.hse.lyubortk.cli.commands.builtins.{Cat, Echo, Exit, Pwd, Wc}
import ru.hse.lyubortk.cli.parsing.PostParser.{AssignmentExpression, Command, PipelineExpression}
import ru.hse.lyubortk.cli.parsing.PreParser.SubstitutionText
import ru.hse.lyubortk.cli.parsing.{PostParser, PreParser}

import scala.collection.mutable
import scala.util.control.NonFatal

object Cli {

  val env: mutable.Map[String, String] = sys.env.to(mutable.Map).withDefault(_ => "")
  val commandExecutor = new CommandExecutor(
    env,
    Map("cat" -> Cat, "echo" -> Echo, "pwd" -> Pwd, "wc" -> Wc, "exit" -> Exit)
  )

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
          case SubstitutionText(text) => env(text)
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
        case AssignmentExpression(identifier, argument) => env.put(identifier.text, argument.text)
        case PipelineExpression(commands) =>
          val lastOutput = commands.foldLeft(Option(InputStream.nullInputStream)) {
            case (input, Command(commandName, arguments)) =>
              if (input.isDefined) {
                val k = commandExecutor.execute(commandName.text, arguments.map(_.text), input.get)
                input.get.close
                k
              } else {
                None
              }
          }
          if (lastOutput.isEmpty) {
            return
          }
          try {
            lastOutput.get.transferTo(System.out)
            System.out.println()
          } catch {
            case NonFatal(e) => System.err.println(e)
          } finally {
            lastOutput.get.close()
          }
      }
  }
}
