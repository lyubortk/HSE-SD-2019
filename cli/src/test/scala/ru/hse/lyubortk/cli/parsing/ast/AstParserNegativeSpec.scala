package ru.hse.lyubortk.cli.parsing.ast

import ru.hse.lyubortk.cli.SpecBase

class AstParserNegativeSpec extends SpecBase {
  "AstParser" should "fail on broken assignment" in {
    AstParser("=b") shouldBe a [Left[_, _]]
    AstParser("a=") shouldBe a [Left[_, _]]
    AstParser("=") shouldBe a [Left[_, _]]
    AstParser("a=  ") shouldBe a [Left[_, _]]
    AstParser(" =b") shouldBe a [Left[_, _]]
    AstParser(" =  ") shouldBe a [Left[_, _]]
  }

  it should "fail on unclosed quotes" in {
    AstParser("cat 'gradle.build ") shouldBe a [Left[_, _]]
    AstParser("echo \"gradle.build\" \"gradle ") shouldBe a [Left[_, _]]
    AstParser("echo \"gradle.build\" \"gradle ") shouldBe a [Left[_, _]]
  }

  it should "fail on broken pipe expression" in {
    AstParser("|") shouldBe a [Left[_, _]]
    AstParser(" | ") shouldBe a [Left[_, _]]
    AstParser("echo hello | ") shouldBe a [Left[_, _]]
    AstParser("| wc") shouldBe a [Left[_, _]]
    AstParser("cat build.gradle | | ") shouldBe a [Left[_, _]]
  }

  it should "fail if assignment is combined with pipes" in {
    AstParser("a=b | cat") shouldBe a [Left[_, _]]
    AstParser("echo hell | a=3") shouldBe a [Left[_, _]]
  }
}
