package ru.hse.lyubortk.cli.parsing.substitution

/**
 * Substitution parser should parse text into a sequence of tokens.
 */
sealed trait Token {
  def text: String
}

object Token {
  /**
   * Just a regular text which could include anything except a substitution.
   */
  case class RegularText(text: String) extends Token

  /**
   * A word preceded with dollar sign which must be substituted by interpreter
   * (dollar sign IS NOT included in the text string).
   */
  case class SubstitutionText(text: String) extends Token
}
