package ru.hse.lyubortk.cli.parsing

import scala.util.parsing.combinator.RegexParsers

trait CliParser[Result] extends RegexParsers {
  def apply(text: String): Either[ParsingError, Result] = parseAll(parser, text) match {
    case NoSuccess(msg, next) => Left(ParsingError(msg, next.source.toString))
    case Success(result, _) => Right(result)
  }

  protected def parser: Parser[Result]
}
