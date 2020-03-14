package ru.hse.lyubortk.cli

import ru.hse.lyubortk.cli.commands.CommandExecutor
import ru.hse.lyubortk.cli.commands.builtins.{Cat, Echo, Exit, Grep, Pwd, Wc}
import ru.hse.lyubortk.cli.parsing.ast.AstParser
import ru.hse.lyubortk.cli.parsing.substitution.SubstitutionParser

/**
 * Creates Cli class with required dependencies and starts it.
 * This class is also responsible for the registration of builtins in CommandExecutor.
 */
object Main {
  def main(args: Array[String]): Unit = {
    val builtins = Map(
      "cat" -> Cat,
      "echo" -> Echo,
      "exit" -> Exit,
      "pwd" -> Pwd,
      "wc" -> Wc,
      "grep" -> Grep
    )
    val cli = new Cli(
      sys.env,
      new CommandExecutor(builtins),
      SubstitutionParser,
      AstParser
    )
    cli.start()
  }
}
