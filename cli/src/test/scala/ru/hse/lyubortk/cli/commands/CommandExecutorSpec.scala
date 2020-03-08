package ru.hse.lyubortk.cli.commands

import java.io.InputStream

import ru.hse.lyubortk.cli.SpecBase
import ru.hse.lyubortk.cli.commands.CommandResult.{Continue, Exit}
import ru.hse.lyubortk.cli.commands.InputStreamOps._
import ru.hse.lyubortk.cli.commands.Utils._

import scala.collection.mutable

class CommandExecutorSpec extends SpecBase {
  "CommandExecutor" should "find and execute builtin commands" in {
    val builtin = new Command {
      var args: Seq[String] = _
      var stdin: InputStream = _
      var env: Seq[(String, String)] = _
      override def execute(args: Seq[String], stdin: InputStream, env: Seq[(String, String)]): CommandResult = {
        this.args = args
        this.stdin = stdin
        this.env = env
        Exit()
      }
    }
    val env = mutable.Map("hello" -> "hi")
    val commandExecutor = new CommandExecutor(Map("mock-command" -> builtin))
    val args = Seq("a", "b", "c")
    val stdin = "hi".inputStream
    val result = commandExecutor.execute("mock-command", args, stdin, env.toSeq)
    result shouldBe a [Exit]
    builtin.args shouldBe theSameInstanceAs (args)
    builtin.stdin shouldBe theSameInstanceAs (stdin)
    builtin.env shouldBe env.toSeq
  }

  it should "execute system commands if builtin was not found" in {
    val variableName = "some_variable"
    val variableValue = "i_am_value"
    val (command, arguments) = if (System.getProperty("os.name").startsWith("Windows")) {
      ("cmd", Seq("/C", s"echo %${variableName}%"))
    } else {
      ("bash", Seq("-c", s"echo $$$variableName"))
    }
    val commandExecutor = new CommandExecutor(Map.empty)
    val result = commandExecutor.execute(command, arguments, env = Seq(variableName -> variableValue))
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    assert(output.contains(variableValue))
    errOutput shouldBe ""
  }

  it should "process unknown commands correctly" in {
    val commandExecutor = new CommandExecutor(Map.empty)
    val result = commandExecutor.execute("this-command-should-not-exist-abacaba")
    result shouldBe a [Continue]
    val (output, errOutput) = extractOutput(result)
    output shouldBe ""
    errOutput shouldNot have length 0 // system dependent
  }
}
