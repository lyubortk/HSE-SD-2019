package ru.hse.lyubortk.cli.parsing.substitution

import ru.hse.lyubortk.cli.parsing.CliParser
import ru.hse.lyubortk.cli.parsing.substitution.Token._

import scala.language.postfixOps

/**
 * Parses text into a sequence of tokens (which could be used to perform substitutions).
 */
object SubstitutionParser extends CliParser[Seq[Token]] {
  override val skipWhitespace = false

  private val doubleQuote: Parser[Token] = "\"".r ^^ Token.RegularText

  private val regularTextInDoubleQuotes: Parser[Token] = """([^"$]|\$(?![^'"|=\s$]))+""".r ^^ RegularText

  private val substitutionText: Parser[SubstitutionText] = """\$[^'"|=\s$]+""".r ^^ { text =>
      SubstitutionText(text.tail)
    }

  private val doubleQuotedText: Parser[Seq[Token]] =
    doubleQuote ~ ((regularTextInDoubleQuotes | substitutionText) *) ~ doubleQuote map {
      case begining ~ insides ~ end => Seq(Seq(begining), insides, Seq(end)).flatten
    }

  private val singleQuotedText: Parser[RegularText] = """'[^']*'""".r ^^ RegularText

  private val plainText: Parser[RegularText] = """([^'"$]|\$(?![^'"|=\s$]))+""".r ^^ RegularText

  private val tokens: Parser[Seq[Token]] =
    ((doubleQuotedText | singleQuotedText.map(Seq(_)) | substitutionText.map(Seq(_)) | plainText.map(Seq(_))) *) map {
      _.flatten
    }

  override def parser: SubstitutionParser.Parser[Seq[Token]] = tokens
}
