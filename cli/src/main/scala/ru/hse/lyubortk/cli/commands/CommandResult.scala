package ru.hse.lyubortk.cli.commands

import java.io.InputStream

sealed trait CommandResult {
  def output: InputStream
  def errOutput: InputStream
}

object CommandResult {
  case class Continue(output: InputStream, errOutput: InputStream) extends CommandResult
  object Continue {
    def apply(output: InputStream): Continue = Continue(output, InputStream.nullInputStream())
    def apply(): Continue = Continue(InputStream.nullInputStream(), InputStream.nullInputStream())
  }

  case class Exit(output: InputStream, errOutput: InputStream) extends CommandResult
  object Exit {
    def apply(output: InputStream): Exit = Exit(output, InputStream.nullInputStream())
    def apply(): Exit = Exit(InputStream.nullInputStream(), InputStream.nullInputStream())
  }

  def unapply(arg: CommandResult): Option[(InputStream, InputStream)] = Some((arg.output, arg.errOutput))
}
