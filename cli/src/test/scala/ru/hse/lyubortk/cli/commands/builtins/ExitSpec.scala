package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import ru.hse.lyubortk.cli.CliSpecBase
import ru.hse.lyubortk.cli.commands.CommandResult
import ru.hse.lyubortk.cli.commands.builtins.Utils._

class ExitSpec extends CliSpecBase {
  "Exit" should "return 'Exit' CommandResult with no output" in {
    val result = Exit.execute(Seq.empty, InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [CommandResult.Exit]
    val (output, errOutput) = extractOutput(result)
    output shouldBe ""
    errOutput shouldBe ""
  }
}
