package ru.hse.lyubortk.cli.parsing.substitution

import ru.hse.lyubortk.cli.SpecBase
import ru.hse.lyubortk.cli.parsing.substitution.Token.{RegularText, SingleQuotedText, SubstitutionText}

class SubstitutionParserPositiveSpec extends SpecBase {
  "SubstitutionParser" should "parse empty string" in {
    SubstitutionParser("") shouldBe Right(Seq.empty)
  }

  it should "parse whitespaces" in {
    SubstitutionParser("\t    \t   \t") shouldBe Right(Seq(RegularText("\t    \t   \t")))
  }

  it should "parse regular substitution" in {
    SubstitutionParser("$hello") shouldBe Right(Seq(SubstitutionText("hello")))
  }

  it should "parse substitution in double quotes" in {
    SubstitutionParser("\"$hello\"") shouldBe Right(Seq(
      RegularText("\""),
      SubstitutionText("hello"),
      RegularText("\"")
    ))
  }

  it should "not parse substitution in single quotes" in {
    SubstitutionParser("'$hello'") shouldBe Right(Seq(SingleQuotedText("'$hello'")))
  }

  it should "parse substitution right after other text" in {
    SubstitutionParser(" echo   hello$name \"bye$name2\" 'hell'$o ") shouldBe Right(Seq(
      RegularText(" echo   hello"),
      SubstitutionText("name"),
      RegularText(" \"bye"),
      SubstitutionText("name2"),
      RegularText("\" "),
      SingleQuotedText("'hell'"),
      SubstitutionText("o"),
      RegularText(" ")
    ))
  }

  it should "parse unicode" in {
    SubstitutionParser("привет") shouldBe Right(Seq(RegularText("привет")))
  }

  it should "parse single dollar sign" in {
    SubstitutionParser("echo $") shouldBe Right(Seq(RegularText("echo $")))
    SubstitutionParser("echo $ ") shouldBe Right(Seq(RegularText("echo $ ")))
  }

  it should "parse two subsequent substitutions" in {
    SubstitutionParser("echo $a$b") shouldBe Right(Seq(
      RegularText("echo "),
      SubstitutionText("a"),
      SubstitutionText("b")
    ))
  }
}
