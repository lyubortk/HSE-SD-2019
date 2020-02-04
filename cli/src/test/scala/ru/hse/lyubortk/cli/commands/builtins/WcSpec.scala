package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import ru.hse.lyubortk.cli.CliSpecBase
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.builtins.Utils._

class WcSpec extends CliSpecBase {
  "Wc" should "process empty stdin" in {
    val result = Wc.execute(Seq.empty, InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe "0 0 0\n"
    errOutput shouldBe ""
  }
}
