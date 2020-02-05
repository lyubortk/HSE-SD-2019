package ru.hse.lyubortk.cli.commands

import java.io.InputStream

/**
 * A base trait for all internal commands (builtins). Must not close the specified stdin stream.
 */
trait Command {
  /**
   * Executes command with the specified arguments, input, and environment variables.
   * Returns its output as two streams (regular and error). Must not throw exceptions.
   */
  def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult
}
