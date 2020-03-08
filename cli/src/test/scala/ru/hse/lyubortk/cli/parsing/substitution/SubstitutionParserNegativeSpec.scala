package ru.hse.lyubortk.cli.parsing.substitution

import ru.hse.lyubortk.cli.SpecBase

class SubstitutionParserNegativeSpec extends SpecBase {
  "SubstitutionParser" should "fail on unclosed single quotes" in {
    SubstitutionParser("cat ' ") shouldBe a [Left[_, _]]
  }

  "SubstitutionParser" should "fail on unclosed double quotes" in {
    SubstitutionParser("cat \" ") shouldBe a [Left[_, _]]
  }
}
