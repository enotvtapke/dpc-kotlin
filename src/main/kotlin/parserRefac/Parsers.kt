package parserRefac

fun interface Parser {
  operator fun invoke(s: String): List<Result>
}

data class Term(val c: Char) : Parser {
  override fun invoke(s: String): List<Result> = if (s.startsWith(c)) {
    listOf(Immediate(listOf(Token(c)), s.removePrefix(c.toString())))
  } else {
    listOf(Failed(listOf(), s))
  }

  override fun toString() = "'$c'"
}

data class Def(val parser: Parser) : Parser {
  override fun invoke(s: String): List<Result> = listOf(Deferred(listOf(), parser, s))
  override fun toString() = "[$parser]"
}

data class Seq(val seq: List<Parser>) : Parser {
  private fun internal(s: String, consumed: List<Token>, seq: List<Parser>): List<Result> = when {
    seq.isEmpty() -> listOf(Immediate(consumed, s))
    else -> {
      val head = seq.first()
      val tail = seq.drop(1)
      head(s).flatMap { res ->
        when (res) {
          is Immediate -> internal(res.remainder, consumed + res.consumed, tail)
          is Failed -> listOf(Failed(consumed + res.consumed, res.remainder))
          is Deferred -> listOf(Deferred(consumed, Seq(listOf(res.parser) + tail), res.remainder))
        }
      }
    }
  }

  override fun invoke(s: String): List<Result> = internal(s, listOf(), seq)
  override fun toString() = "(" + seq.joinToString(" \u22B3 ") + ")"
}

data class Alt(val alts: List<Seq>) : Parser {
  override fun invoke(s: String): List<Result> = alts.flatMap { it(s) }
  override fun toString() = alts.joinToString(" | ")
}
