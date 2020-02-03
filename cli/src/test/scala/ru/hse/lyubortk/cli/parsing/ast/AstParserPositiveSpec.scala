package ru.hse.lyubortk.cli.parsing.ast

import ru.hse.lyubortk.cli.CliSpecBase
import ru.hse.lyubortk.cli.parsing.ast.Expression._

class AstParserPositiveSpec extends CliSpecBase {
  "AstParser" should "parse empty string" in {
    AstParser("") shouldBe Right(PipelineExpression(Seq.empty))
  }

  it should "parse whitespaces" in {
    AstParser("\t    \t   \t") shouldBe Right(PipelineExpression(Seq.empty))
  }

  it should "parse simple assignment" in {
    AstParser("a=b") shouldBe Right(AssignmentExpression(Word("a"), Word("b")))
  }

  it should "parse assignment with single-quoted text" in {
    AstParser("var='hello there'") shouldBe Right(AssignmentExpression(Word("var"), QuotedText("hello there")))
  }

  it should "parse assignment with double-quoted text" in {
    AstParser("var=\"echo cat\"") shouldBe Right(AssignmentExpression(Word("var"), QuotedText("echo cat")))
  }

  it should "parse simple command without arguments" in {
    AstParser("pwd") shouldBe Right(PipelineExpression(Seq(Command(Word("pwd"), Seq.empty))))
  }

  it should "parse multi-word command without arguments" in {
    AstParser("'multi word command'") shouldBe
      Right(PipelineExpression(Seq(Command(QuotedText("multi word command"), Seq.empty))))
  }

  it should "parse command with arguments" in {
    AstParser("echo hello 'hello!' \"hello!!\" 'hey hey'") shouldBe Right(PipelineExpression(Seq(
      Command(Word("echo"), Seq(Word("hello"), QuotedText("hello!"), QuotedText("hello!!"), QuotedText("hey hey")))
    )))
  }

  it should "drop extra spaces" in {
    AstParser("  \t a=33  \t ") shouldBe Right(AssignmentExpression(Word("a"), Word("33")))
    AstParser("  echo hello | wc ") shouldBe Right(PipelineExpression(Seq(
      Command(Word("echo"), Seq(Word("hello"))),
      Command(Word("wc"), Seq.empty)
    )))
  }

  it should "parse pipeline" in {
    AstParser("cat build.gradle | wc | wc 'build.gradle'") shouldBe Right(PipelineExpression(Seq(
      Command(Word("cat"), Seq(Word("build.gradle"))),
      Command(Word("wc"), Seq.empty),
      Command(Word("wc"), Seq(QuotedText("build.gradle")))
    )))
  }

  it should "parse quotes without text inside" in {
    AstParser("'' \"\" '' \"\"") shouldBe
      Right(PipelineExpression(Seq(Command(QuotedText(""), Seq(QuotedText(""), QuotedText(""), QuotedText(""))))))
  }

  it should "parse unicode" in {
    AstParser("привет") shouldBe Right(PipelineExpression(Seq(Command(Word("привет"), Seq.empty))))
  }
}
