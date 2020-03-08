package ru.hse.lyubortk.cli

import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
abstract class SpecBase extends AnyFlatSpec with Matchers {
  protected val ResourcesFolder = "src/test/resources"
  protected val EmptyFile = s"$ResourcesFolder/empty_file.test"
  protected val MultiLineFile = s"$ResourcesFolder/multi_line_file.test"
  protected val SimpleFile = s"$ResourcesFolder/simple_file.test"
  protected val NonExistentFile = s"$ResourcesFolder/no_file_here"
  protected val BinaryFile = s"$ResourcesFolder/binary_file.test"
}
