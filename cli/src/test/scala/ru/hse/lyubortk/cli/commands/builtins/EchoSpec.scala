package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import ru.hse.lyubortk.cli.CliSpecBase
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.builtins.Utils._

class EchoSpec extends CliSpecBase {
  "Echo" should "print zero arguments" in {
    val result = Echo.execute(Seq.empty, InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe "\n"
    errOutput shouldBe ""
  }

  it should "print one argument" in {
    val result = Echo.execute(Seq(" hello there "), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe " hello there \n"
    errOutput shouldBe ""
  }

  it should "print multiple arguments" in {
    val result = Echo.execute(Seq("abacaba", "hi ", " hey"), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe "abacaba hi   hey\n"
    errOutput shouldBe ""
  }
}
