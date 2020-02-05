package ru.hse.lyubortk.cli.parsing.ast

/**
 * Base AST trait: every valid input is an Expression.
 */
sealed trait Expression

object Expression {

  /**
   * A sequence of commands separated with pipelines (could also be 0 or 1 command).
   */
  case class PipelineExpression(commands: Seq[Command]) extends Expression

  /**
   * Environment variable assignment (ex: "a=123").
   */
  case class AssignmentExpression(identifier: Word, argument: Text) extends Expression

  /**
   * A single command.
   */
  case class Command(commandName: Text, arguments: Seq[Text])

  /**
   * Leaves of the AST.
   */
  sealed trait Text {
    def text: String
  }

  object Text {
    def unapply(arg: Text): Option[String] = Some(arg.text)
  }

  /**
   * A text wrapped between single or double quotes. Quotes are NOT included in the text string.
   */
  case class QuotedText(text: String) extends Text

  /**
   * A regular word (without whitespaces).
   */
  case class Word(text: String) extends Text

}

