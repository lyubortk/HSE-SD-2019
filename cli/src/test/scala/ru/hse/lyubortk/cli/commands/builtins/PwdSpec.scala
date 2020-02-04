package ru.hse.lyubortk.cli.commands.builtins

import java.io.InputStream
import java.nio.file.Paths

import org.apache.commons.io.input.CountingInputStream
import ru.hse.lyubortk.cli.CliSpecBase
import ru.hse.lyubortk.cli.commands.builtins.Utils._
import ru.hse.lyubortk.cli.commands.CommandResult.Continue
import ru.hse.lyubortk.cli.commands.InputStreamOps._

class PwdSpec extends CliSpecBase {
  "Pwd" should "print correct directory" in {
    val result = Pwd.execute(Seq.empty, InputStream.nullInputStream(), Seq.empty)
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe (Paths.get(".").normalize().toAbsolutePath + "\n")
    errOutput shouldBe ""
  }

  it should "not read from stdin" in {
    val stdin = new CountingInputStream("hello!".inputStream)
    Pwd.execute(Seq.empty, InputStream.nullInputStream(), Seq.empty)
    Pwd.execute(Seq("aaaa"), stdin, Seq.empty)
    stdin.getCount shouldBe 0
  }
}
