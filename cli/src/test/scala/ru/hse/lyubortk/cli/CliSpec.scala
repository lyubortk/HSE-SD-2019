package ru.hse.lyubortk.cli

import java.io.{BufferedReader, ByteArrayOutputStream, InputStream, InputStreamReader, PrintStream}
import java.nio.charset.Charset

import org.apache.commons.io.IOUtils
import ru.hse.lyubortk.cli.Cli.IO
import ru.hse.lyubortk.cli.commands.CommandResult.{Continue, Exit}
import ru.hse.lyubortk.cli.commands.{CommandExecutor, CommandResult}
import ru.hse.lyubortk.cli.parsing.CliParser
import ru.hse.lyubortk.cli.parsing.ast.Expression
import ru.hse.lyubortk.cli.parsing.ast.Expression.{AssignmentExpression, Command, PipelineExpression, Word}
import ru.hse.lyubortk.cli.parsing.substitution.Token
import ru.hse.lyubortk.cli.parsing.substitution.Token.{RegularText, SubstitutionText}
import ru.hse.lyubortk.cli.commands.InputStreamOps._

import scala.collection.mutable

class CliSpec extends SpecBase {
  private val dummySubstitutionParser: CliParser[Seq[Token]] = new CliParser[Seq[Token]] {
    override protected def parser: Parser[Seq[Token]] = """.*""".r ^^ (text => Seq(RegularText(text)))
  }

  "Cli" should "not output anything and not call command executor on empty input " in {

    val astParser: CliParser[Expression] = new CliParser[Expression] {
      override protected def parser: Parser[Expression] = """.*""".r ^^ (_ => PipelineExpression(Seq.empty))
    }
    val commandExecutorBuilder = { env: mutable.Map[String, String] => new CommandExecutor(env, Map.empty) {
      override def execute(command: String, arguments: Seq[String], stdin: InputStream): CommandResult = {
        fail("cli called command executor")
        Exit()
      }
    }}

    val (byteArrayOutputStream, printStream, io) = generateIO("\t\t\t    \t\n\n  \n    \n \t \n")

    val cli = new Cli(Map.empty, commandExecutorBuilder, dummySubstitutionParser, astParser, io)
    cli.start()
    printStream.flush()
    byteArrayOutputStream.flush()
    byteArrayOutputStream.toString shouldBe ""
  }

  it should "process pipeline expressions correctly" in {

    val astParser: CliParser[Expression] = new CliParser[Expression] {
      var invocationCount = 0
      override protected def parser: Parser[Expression] = """.*""".r ^^ {_ =>
        invocationCount shouldBe 0
        invocationCount += 1
        PipelineExpression(Seq(
          Command(Word("1"), Seq.empty),
          Command(Word("2"), Seq.empty),
          Command(Word("3"), Seq.empty)
        ))
      }
    }

    val commandExecutorBuilder = { env: mutable.Map[String, String] => new CommandExecutor(env, Map.empty) {
      override def execute(command: String, arguments: Seq[String], stdin: InputStream): CommandResult = {
        val input = IOUtils.toString(stdin, Charset.defaultCharset())
        if (command == "1") {
          input shouldBe ""
          Continue("first command output".inputStream)
        } else if (command == "2") {
          input shouldBe "first command output"
          Continue("second command output".inputStream)
        } else if (command == "3") {
          input shouldBe "second command output"
          Continue("final output".inputStream)
        } else {
          fail("cli passed unknown command to command executor")
        }
      }
    }}

    val (byteArrayOutputStream, printStream, io) = generateIO("1 | 2 | 3")

    val cli = new Cli(Map.empty, commandExecutorBuilder, dummySubstitutionParser, astParser, io)
    cli.start()
    printStream.flush()
    byteArrayOutputStream.toString shouldBe "final output"
  }

  it should "not execute commands after exit" in {

    val astParser: CliParser[Expression] = new CliParser[Expression] {
      override protected def parser: Parser[Expression] = """.*""".r ^^ {_ =>
        PipelineExpression(Seq(
          Command(Word("1"), Seq.empty),
          Command(Word("exit"), Seq.empty),
          Command(Word("3"), Seq.empty)
        ))
      }
    }

    val commandExecutorBuilder = { env: mutable.Map[String, String] => new CommandExecutor(env, Map.empty) {
      override def execute(command: String, arguments: Seq[String], stdin: InputStream): CommandResult = {
        if (command == "1") {
          Continue()
        } else if (command == "exit") {
          Exit()
        } else {
          fail("cli executed command after exit")
        }
      }
    }}

    val (byteArrayOutputStream, printStream, io) = generateIO("1 | exit | 3\n4 \n 5 | 6")
    val cli = new Cli(Map.empty, commandExecutorBuilder, dummySubstitutionParser, astParser, io)
    cli.start()
    printStream.flush()
    byteArrayOutputStream.toString shouldBe ""
  }

  it should "process assignments and substitutions" in {

    val dummySubstitutionParser: CliParser[Seq[Token]] = new CliParser[Seq[Token]] {
      override protected def parser: Parser[Seq[Token]] =
        ("a=10" ^^ (text => Seq(RegularText(text)))) |
          ("echo1 $a" ^^ (_ => Seq(RegularText("echo1 "), SubstitutionText("a")))) |
          ("a=20" ^^ (text => Seq(RegularText(text)))) |
          ("echo2 $a"^^ (_ => Seq(RegularText("echo2 "), SubstitutionText("a"))))
    }

    val astParser: CliParser[Expression] = new CliParser[Expression] {
      override protected def parser: Parser[Expression] =
        ("a=10" ^^ (_ => AssignmentExpression(Word("a"), Word("10")))) |
          ("echo1 10" ^^ (_ => PipelineExpression(Seq(Command(Word("echo1"), Seq(Word("10"))))))) |
          ("a=20" ^^ (_ => AssignmentExpression(Word("a"), Word("20")))) |
          ("echo2 20" ^^ (_ => PipelineExpression(Seq(Command(Word("echo2"), Seq(Word("20")))))))
    }

    val commandExecutorBuilder = { env: mutable.Map[String, String] => new CommandExecutor(env, Map.empty) {
      override def execute(command: String, arguments: Seq[String], stdin: InputStream): CommandResult = {
        if (command == "echo1") {
          arguments shouldBe Seq("10")
          Continue("ok1".inputStream.withNewline)
        } else if (command == "echo2") {
          arguments shouldBe Seq("20")
          Continue("ok2".inputStream.withNewline)
        } else {
          fail("cli passed unknown command to command executor")
        }
      }
    }}

    val (byteArrayOutputStream, printStream, io) = generateIO(
      """a=10
        |echo1 $a
        |a=20
        |echo2 $a
        |""".stripMargin
    )
    val cli = new Cli(Map.empty, commandExecutorBuilder, dummySubstitutionParser, astParser, io)
    cli.start()
    printStream.flush()
    byteArrayOutputStream.toString shouldBe "ok1\nok2\n"
  }

  private def generateIO(input: String): (ByteArrayOutputStream, PrintStream, IO) = {
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val printStream = new PrintStream(byteArrayOutputStream)
    val io = IO(
      printStream,
      printStream,
      new BufferedReader(new InputStreamReader(input.inputStream))
    )
    (byteArrayOutputStream, printStream, io)
  }
}
