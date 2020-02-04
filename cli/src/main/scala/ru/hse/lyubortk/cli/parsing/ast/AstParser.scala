package ru.hse.lyubortk.cli.parsing.ast

import ru.hse.lyubortk.cli.parsing.CliParser
import ru.hse.lyubortk.cli.parsing.ast.Expression._

import scala.language.postfixOps

object AstParser extends CliParser[Expression] {
  private val quotedText: Parser[QuotedText] = """'[^']*'|"[^"]*"""".r ^^ { text =>
    QuotedText(text.tail.init)
  }
  private val word: Parser[Word] = """[^\s'"|=]+""".r ^^ Word
  private val pipeline: Parser[Unit] = "|" ^^ (_ => ())
  private val equalsSign: Parser[Unit] = "=" ^^ (_ => ())
  private val text: Parser[Text] = quotedText | word

  private val command: Parser[Command] = text ~ (text*) ^^ {
    case commandName ~ arguments => Command(commandName, arguments)
  }

  private val pipelineExpression: Parser[PipelineExpression] = command ~ ((pipeline ~> command)*) ^^ {
    case firstCommand ~ otherCommands => PipelineExpression(firstCommand :: otherCommands)
  }
  private val assignmentExpression: Parser[AssignmentExpression] = (word <~ equalsSign) ~ text ^^ {
    case identifier ~ text => AssignmentExpression(identifier, text)
  }
  private val emptyPipelineExpression: Parser[PipelineExpression] = "" ^^ (_ => PipelineExpression(Seq.empty))

  private val expression: Parser[Expression] = assignmentExpression | pipelineExpression | emptyPipelineExpression

  override def parser: AstParser.Parser[Expression] = expression
}
