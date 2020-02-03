package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream
import java.nio.file.Paths

import ru.hse.lyubortk.cli.CliSpecBase
import ru.hse.lyubortk.cli.commands.builtins.Utils._
import ru.hse.lyubortk.cli.commands.CommandResult.Continue

class PwdSpec extends CliSpecBase {
  "Pwd" should "print correct directory" in {
    val result = Pwd.execute(Seq.empty, InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe (Paths.get(".").normalize().toAbsolutePath + "\n")
    errOutput shouldBe ""
  }
}
