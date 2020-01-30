package ru.hse.lyubortk.cli.parsing

import scala.util.parsing.combinator.RegexParsers
import scala.language.postfixOps

object PreParser extends RegexParsers {
  override val skipWhitespace = false

  private val singleQuotedText: Parser[SingleQuotedText] = """'[^']*'""".r ^^ SingleQuotedText
  private val regularText: Parser[RegularText] = """([^'$]|\$\s|\$$)+""".r ^^ RegularText
  private val substitutionText: Parser[SubstitutionText] = """\$[^\s'"]+""".r ^^ { text =>
    SubstitutionText(text.tail)
  }
  private val tokens: Parser[Seq[Token]] = (singleQuotedText | regularText | substitutionText)*

  def apply(text: String): Either[RuntimeException, Seq[Token]] = parseAll(tokens, text) match {
    case NoSuccess(msg, _) => Left(new RuntimeException(msg))
    case Success(result, _) => Right(result)
  }

  sealed trait Token {
    def text: String
  }

  case class SingleQuotedText(text: String) extends Token
  case class RegularText(text: String) extends Token
  case class SubstitutionText(text: String) extends Token
}
