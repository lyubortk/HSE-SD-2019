package ru.hse.lyubortk.cli.commands

import java.io.{ByteArrayInputStream, InputStream, SequenceInputStream}

private[commands] object InputStreamOps {
  implicit class NewLineAdder(val inputStream: InputStream) extends AnyVal {
    def withNewline: InputStream = {
      new SequenceInputStream(inputStream, new ByteArrayInputStream("\n".getBytes))
    }
  }

  implicit class ByteInputStreamCreator(val text: String) extends AnyVal {
    def inputStream: InputStream = new ByteArrayInputStream(text.getBytes)
  }
}

