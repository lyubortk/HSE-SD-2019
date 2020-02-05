package ru.hse.lyubortk.cli.parsing.substitution

/**
 * Substitution parser should parse text into a sequence of tokens.
 */
sealed trait Token {
  def text: String
}

object Token {

  /**
   * Text wrapped in single quotes (quotes will still be included in the text string).
   */
  case class SingleQuotedText(text: String) extends Token

  /**
   * Just a regular text which could include multiple words and double quotes.
   */
  case class RegularText(text: String) extends Token

  /**
   * A word preceded with dollar sign which must be substituted by interpreter
   * (dollar sign IS NOT included in the text string).
   */
  case class SubstitutionText(text: String) extends Token
}
