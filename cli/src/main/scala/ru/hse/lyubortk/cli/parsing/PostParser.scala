package ru.hse.lyubortk.cli.parsing

import scala.util.parsing.combinator.RegexParsers
import scala.language.postfixOps

object PostParser extends RegexParsers {
  private val quotedText: Parser[QuotedText] = """'[^']*'|"[^"]*"""".r ^^ { text =>
    QuotedText(text.tail.init)
  }
  private val word: Parser[Word] = """[^\s'"]+""".r ^^ Word
  private val pipeline: Parser[Unit] = """|""".r ^^ (_ => ())
  private val equalsSign: Parser[Unit] = """=""".r ^^ (_ => ())
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
  private val expression: Parser[Expression] = pipelineExpression | assignmentExpression

  def apply(text: String): Either[RuntimeException, Expression] = parseAll(expression, text) match {
    case NoSuccess(msg, _) => Left(new RuntimeException(msg))
    case Success(result, _) => Right(result)
  }

  sealed trait Text {
    def text: String
  }

  case class QuotedText(text: String) extends Text
  case class Word(text: String) extends Text

  sealed trait Expression
  case class PipelineExpression(commands: Seq[Command]) extends Expression
  case class AssignmentExpression(identifier: Word, argument: Text) extends Expression
  case class Command(commandName: Text, arguments: Seq[Text])
}
