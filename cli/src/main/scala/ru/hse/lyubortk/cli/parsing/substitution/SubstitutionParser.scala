package ru.hse.lyubortk.cli.parsing.substitution

import ru.hse.lyubortk.cli.parsing.CliParser
import ru.hse.lyubortk.cli.parsing.substitution.Token._

import scala.language.postfixOps

object SubstitutionParser extends CliParser[Seq[Token]] {
  override val skipWhitespace = false

  private val singleQuotedText: Parser[SingleQuotedText] = """'[^']*'""".r ^^ SingleQuotedText
  private val regularText: Parser[RegularText] = """([^'$]|\$\s|\$$)+""".r ^^ RegularText
  private val substitutionText: Parser[SubstitutionText] = """\$[^\s$'"]+""".r ^^ { text =>
    SubstitutionText(text.tail)
  }
  private val tokens: Parser[Seq[Token]] = (singleQuotedText | regularText | substitutionText)*

  override def parser: SubstitutionParser.Parser[Seq[Token]] = tokens
}
