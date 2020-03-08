package ru.hse.lyubortk.cli.commands

import java.io.InputStream

/**
 * The result of the executed command.
 */
sealed trait CommandResult {
  /**
   * A stream for regular output.
   */
  def output: InputStream

  /**
   * A stream for error output.
   */
  def errOutput: InputStream
}

object CommandResult {

  /**
   * A CommandResult which indicates that other commands can be executed after this command finishes.
   */
  case class Continue(output: InputStream, errOutput: InputStream) extends CommandResult
  object Continue {
    def apply(output: InputStream): Continue = Continue(output, InputStream.nullInputStream())
    def apply(): Continue = Continue(InputStream.nullInputStream(), InputStream.nullInputStream())
  }

  /**
   * A CommandResult which indicates that no more commands can be executed (interpreter must stop).
   */
  case class Exit(output: InputStream, errOutput: InputStream) extends CommandResult
  object Exit {
    def apply(output: InputStream): Exit = Exit(output, InputStream.nullInputStream())
    def apply(): Exit = Exit(InputStream.nullInputStream(), InputStream.nullInputStream())
  }

  def unapply(arg: CommandResult): Option[(InputStream, InputStream)] = Some((arg.output, arg.errOutput))
}
