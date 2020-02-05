package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import org.apache.commons.io.input.CountingInputStream
import ru.hse.lyubortk.cli.SpecBase
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.Utils._

class CatSpec extends SpecBase {
  "Cat" should "print one file with one line" in {
    val result = Cat.execute(Seq(SimpleFile), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe "hello!\n"
    errOutput shouldBe ""
  }

  it should "print multiline file" in {
    val result = Cat.execute(Seq(MultiLineFile), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe "this is\nmultiline\nfile\n"
    errOutput shouldBe ""
  }

  it should "print empty file" in {
    val result = Cat.execute(Seq(EmptyFile), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe "\n"
    errOutput shouldBe ""
  }

  it should "print multiple files" in {
    val result = Cat.execute(Seq(
      SimpleFile,
      SimpleFile,
      MultiLineFile,
      EmptyFile
    ), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe "hello!\nhello!\nthis is\nmultiline\nfile\n\n"
    errOutput shouldBe ""
  }

  it should "not read from stdin" in {
    val stdin = new CountingInputStream("hello!".inputStream)
    Cat.execute(Seq(SimpleFile), stdin, Seq.empty)
    Cat.execute(Seq.empty, stdin, Seq.empty)
    stdin.getCount shouldBe 0
  }

  it should "print error if file does not exist" in {
    val result = Cat.execute(Seq(NonExistentFile), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe ""
    errOutput shouldNot have length 0 // text is platform dependent
  }

  it should "print existing files even if some arguments are invalid" in {
    val result = Cat.execute(Seq(
      SimpleFile,
      NonExistentFile,
      SimpleFile
    ), InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe "hello!\nhello!\n"
    errOutput shouldNot have length 0
  }
}
