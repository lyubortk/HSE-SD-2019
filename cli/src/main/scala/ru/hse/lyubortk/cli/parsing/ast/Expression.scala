package ru.hse.lyubortk.cli.parsing.ast

sealed trait Expression

object Expression {
  case class PipelineExpression(commands: Seq[Command]) extends Expression
  case class AssignmentExpression(identifier: Word, argument: Text) extends Expression

  case class Command(commandName: Text, arguments: Seq[Text])

  sealed trait Text {
    def text: String
  }

  object Text {
    def unapply(arg: Text): Option[String] = Some(arg.text)
  }

  case class QuotedText(text: String) extends Text
  case class Word(text: String) extends Text
}

