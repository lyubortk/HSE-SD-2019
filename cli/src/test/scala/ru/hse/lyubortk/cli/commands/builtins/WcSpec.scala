package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import org.apache.commons.io.input.CountingInputStream
import ru.hse.lyubortk.cli.SpecBase
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.Utils._

class WcSpec extends SpecBase {
  "Wc" should "process empty stdin" in {
    val result = Wc.execute(Seq.empty, InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe "0 0 0\n"
    errOutput shouldBe ""
  }

  it should "process stdin with input" in {
    val input = "1 3  \t6\n89\na"
    val result = Wc.execute(Seq.empty, input.inputStream, Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe s"3 5 ${input.getBytes().length}\n"
    errOutput shouldBe ""
  }

  it should "process one argument" in {
    val result = Wc.execute(Seq(MultiLineFile), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe s"3 4 22 $MultiLineFile\n"
    errOutput shouldBe ""
  }

  it should "process multiple arguments" in {
    val result = Wc.execute(Seq(MultiLineFile, SimpleFile), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe s"3 4 22 $MultiLineFile\n1 1 6 $SimpleFile\n4 5 28 total\n"
    errOutput shouldBe ""
  }

  it should "not read from stdin" in {
    val stdin = new CountingInputStream("hello!".inputStream)
    Wc.execute(Seq(EmptyFile), stdin, Seq.empty)
    Wc.execute(Seq(MultiLineFile, SimpleFile), stdin, Seq.empty)
    stdin.getCount shouldBe 0
  }

  it should "process existing files even if some arguments are invalid" in {
    val result = Wc.execute(Seq(
      SimpleFile,
      NonExistentFile,
      BinaryFile,
      SimpleFile
    ), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe s"1 1 6 $SimpleFile\n1 1 6 $SimpleFile\n2 2 12 total\n"
    errOutput shouldNot have length 0
    assert(errOutput.contains(Wc.CharsetErrorMessage))
  }

  it should "fail on files with wrong encoding" in {
    val result = Wc.execute(Seq(BinaryFile), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe ""
    errOutput shouldBe s"${Wc.CharsetErrorMessage}\n"
  }
}
