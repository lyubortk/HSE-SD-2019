package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import org.apache.commons.io.input.CountingInputStream
import ru.hse.lyubortk.cli.CliSpecBase
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.builtins.Utils._
import ru.hse.lyubortk.cli.commands.InputStreamOps._

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

  it should "not read from stdin" in {
    val stdin = new CountingInputStream("hello!".inputStream)
    Echo.execute(Seq.empty, stdin, Seq.empty)
    Echo.execute(Seq("ab", "ca"), stdin, Seq.empty)
    stdin.getCount shouldBe 0
  }
}
