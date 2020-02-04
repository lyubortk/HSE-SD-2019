package ru.hse.lyubortk.cli

import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
abstract class CliSpecBase extends AnyFlatSpec with Matchers {
  protected val ResourcesFolder = "src/test/resources"
}
