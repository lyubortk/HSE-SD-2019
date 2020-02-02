package ru.hse.lyubortk.cli.commands

import java.io.InputStream

sealed trait CommandResult {
  def output: InputStream
}

object CommandResult {
  case class Exit(output: InputStream) extends CommandResult
  case class Continue(output: InputStream) extends CommandResult

  def unapply(arg: CommandResult): Option[InputStream] = Some(arg.output)
}
