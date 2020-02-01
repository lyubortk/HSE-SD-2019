package ru.hse.lyubortk.cli.commands

import java.io.InputStream

trait Command {
  def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): Option[InputStream]
}
