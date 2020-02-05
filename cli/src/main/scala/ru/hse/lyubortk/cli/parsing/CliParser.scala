package ru.hse.lyubortk.cli.parsing

import scala.util.parsing.combinator.RegexParsers

/**
 * Base trait for all cli parsers.
 *
 * @tparam Result successful parsing result
 */
trait CliParser[Result] extends RegexParsers {
  /**
   * Tries to parse the specified text into a Result (type parameter).
   */
  def apply(text: String): Either[ParsingError, Result] = parseAll(parser, text) match {
    case NoSuccess(msg, next) => Left(ParsingError(msg, next.source.toString))
    case Success(result, _) => Right(result)
  }

  protected def parser: Parser[Result]
}
