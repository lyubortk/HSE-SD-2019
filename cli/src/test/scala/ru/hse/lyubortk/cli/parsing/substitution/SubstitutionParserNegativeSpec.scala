package ru.hse.lyubortk.cli.parsing.substitution

import ru.hse.lyubortk.cli.CliSpecBase

class SubstitutionParserNegativeSpec extends CliSpecBase {
  "SubstitutionParser" should "fail on unclosed single quotes" in {
    SubstitutionParser("cat ' ") shouldBe a [Left[_, _]]
  }

  "SubstitutionParser" should "fail on double multiple subsequent dollar signs" in {
    SubstitutionParser("cat $$ ") shouldBe a [Left[_, _]]
    SubstitutionParser("cat $$$ a") shouldBe a [Left[_, _]]
  }
}
