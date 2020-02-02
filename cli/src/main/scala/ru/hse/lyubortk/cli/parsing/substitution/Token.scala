package ru.hse.lyubortk.cli.parsing.substitution

sealed trait Token {
  def text: String
}

object Token {
  case class SingleQuotedText(text: String) extends Token
  case class RegularText(text: String) extends Token
  case class SubstitutionText(text: String) extends Token
}
