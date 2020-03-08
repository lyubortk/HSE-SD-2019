package ru.hse.lyubortk.cli.parsing

/**
 * This class is returned by CliParsers if parsing fails.
 *
 * @param message      parser error message
 * @param unparsedText text which was left unparsed
 */
case class ParsingError(message: String, unparsedText: String)
