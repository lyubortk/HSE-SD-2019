package ru.hse.lyubortk.cli.commands.builtins

import org.apache.commons.io.input.CountingInputStream
import ru.hse.lyubortk.cli.SpecBase
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.Utils._

class GrepSpec extends SpecBase {

  import GrepSpec._

  "Grep" should "find simple regex from stdin" in {
    val result = Grep.execute(Seq("hello"), StandardInput.inputStream, Seq.empty)
    result shouldBe a[Continue]
    val (output, errOutput) = extractOutput(result)
    val expected =
      """hello
        |aa hello aa
        |""".stripMargin

    output shouldBe expected
    errOutput shouldBe ""
  }

  it should "find case insensitive regex" in {
    val result = Grep.execute(Seq("-i", "hEllo"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    val expected =
      """hello
        |abHEllO_b
        |aa hello aa
        |""".stripMargin

    output shouldBe expected
    errOutput shouldBe ""
  }

  it should "work with 'after context' option" in {
    val result = Grep.execute(Seq("-A", "1", "abacaba"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    val expected =
      """abacaba
        |abHEllO_b
        |""".stripMargin
    output shouldBe expected
    errOutput shouldBe ""
  }

  it should "not work if after-context is specified multiple times" in {
    val result = Grep.execute(Seq("-A", "3", "-A", "2", "-A", "1", "abacaba"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    output shouldBe ""
    errOutput shouldNot have size 0
  }

  it should "not work if flags are specified multiple times" in {
    val result = Grep.execute(Seq("-i", "-w", "-i", "-w", "hEllo"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    output shouldBe ""
    errOutput shouldNot have length 0
  }

  it should "work if 'after context' exceeds last line" in {
    val result = Grep.execute(Seq("--after-context", "10", "hey"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    val expected =
      """hey!
        |aa hello aa
        |aca hey
        |""".stripMargin
    output shouldBe expected
    errOutput shouldBe ""
  }

  it should "reject negative 'after context' parameter" in {
    val result = Grep.execute(Seq("-A", "-1", "a"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    output shouldBe ""
    errOutput shouldNot have length 0
  }

  it should "find whole words" in {
    val result = Grep.execute(Seq("-w", "aca"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    val expected =
      """aca
        | aca j
        |aca hey
        |""".stripMargin
    output shouldBe expected
    errOutput shouldBe ""
  }

  it should "work with all options specified" in {
    val result = Grep.execute(
      Seq("--after-context", "1", "--ignore-case", "--word", "Hello"),
      StandardInput.inputStream,
      Seq.empty
    )
    val (output, errOutput) = extractOutput(result)
    val expected =
      """hello
        |abacaba
        |aa hello aa
        |aca hey
        |""".stripMargin
    output shouldBe expected
    errOutput shouldBe ""
  }

  it should "reject unknown arguments" in {
    val result = Grep.execute(Seq("-b", "a"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    output shouldBe ""
    errOutput shouldNot have length 0
  }

  it should "fail if pattern was not specified" in {
    val result = Grep.execute(Seq("-i"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    output shouldBe ""
    errOutput shouldNot have length 0
  }

  it should "not search for pattern if help was called" in {
    val result = Grep.execute(Seq("--help"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    output shouldNot have length 0
    errOutput shouldBe ""
  }

  it should "search for complex regex" in {
    val result = Grep.execute(Seq("hey|hello"), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    val expected =
      """hello
        |hey!
        |aa hello aa
        |aca hey
        |""".stripMargin
    output shouldBe expected
    errOutput shouldBe ""
  }

  it should "search for regex with asterisk" in {
    val result = Grep.execute(Seq(" (.*) "), StandardInput.inputStream, Seq.empty)
    val (output, errOutput) = extractOutput(result)
    val expected =
      """ aca j
        |aa hello aa
        |""".stripMargin
    output shouldBe expected
    errOutput shouldBe ""
  }

  it should "not read from stdin if filenames are specified" in {
    val stdin = new CountingInputStream("hello!".inputStream)
    Grep.execute(Seq(" (.*) ", EmptyFile), stdin.inputStream, Seq.empty)
    stdin.getCount shouldBe 0
  }

  it should "process files in arguments if they are given" in {
    val result = Grep.execute(
      Seq("[^f]il|hello", MultiLineFile, SimpleFile, EmptyFile),
      StandardInput.inputStream,
      Seq.empty
    )
    val (output, errOutput) = extractOutput(result)
    val expected =
      """multiline
        |hello!
        |
        |""".stripMargin
    output shouldBe expected
    errOutput shouldBe ""
  }
}

object GrepSpec {
  private[builtins] val StandardInput =
    """aca
      | aca j
      |hello
      |abacaba
      |abHEllO_b
      |hey!
      |aa hello aa
      |aca hey
      |""".stripMargin
}
