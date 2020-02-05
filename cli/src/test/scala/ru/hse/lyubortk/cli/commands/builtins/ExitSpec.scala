package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream

import org.apache.commons.io.input.CountingInputStream
import ru.hse.lyubortk.cli.SpecBase
import ru.hse.lyubortk.cli.commands.CommandResult
import ru.hse.lyubortk.cli.commands.Utils._
import ru.hse.lyubortk.cli.commands.InputStreamOps._

class ExitSpec extends SpecBase {
  "Exit" should "return 'Exit' CommandResult with no output" in {
    val result = Exit.execute(Seq.empty, InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [CommandResult.Exit]
    val (output, errOutput) = extractOutput(result)
    output shouldBe ""
    errOutput shouldBe ""
  }

  it should "not read from stdin" in {
    val stdin = new CountingInputStream("hello!".inputStream)
    Exit.execute(Seq.empty, stdin, Seq.empty)
    Exit.execute(Seq("aaaa"), stdin, Seq.empty)
    stdin.getCount shouldBe 0
  }
}
