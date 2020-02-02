package ru.hse.lyubortk.cli

import ru.hse.lyubortk.cli.commands.CommandExecutor
import ru.hse.lyubortk.cli.commands.builtins.{Cat, Echo, Exit, Pwd, Wc}
import ru.hse.lyubortk.cli.parsing.ast.AstParser
import ru.hse.lyubortk.cli.parsing.substitution.SubstitutionParser

object Main {
  def main(args: Array[String]): Unit = {
    val builtins = Map(
      "cat" -> Cat,
      "echo" -> Echo,
      "exit" -> Exit,
      "pwd" -> Pwd,
      "wc" -> Wc
    )
    val cli = new Cli(
      sys.env,
      env => new CommandExecutor(env, builtins),
      SubstitutionParser,
      AstParser
    )
    cli.start()
  }
}
