package parser3

fun interface Parser {
  operator fun invoke(s: String): List<Result>
}

data class Term(val term: String) : Parser {
  override fun invoke(s: String) = if (s.startsWith(term)) listOf(Immediate(s.removePrefix(term))) else listOf()
  override fun toString() = "'$term'"
}

data object Eps : Parser {
  override fun invoke(s: String) = listOf(Immediate(s))
  override fun toString() = "\u03B5"
}

data class Def(val parser: Parser) : Parser {
  override fun invoke(s: String) = listOf(Deferred(parser, s))
  override fun toString() = "[$parser]"
}

data class Seq(val seq: List<Parser>) : Parser {
  private fun internal(s: String, seqTail: List<Parser>): List<Result> =
    when {
      seqTail.isEmpty() -> listOf(Immediate(s))
      else -> seqTail.first()(s).flatMap {
        when (it) {
          is Immediate -> internal(it.remainder, seqTail.drop(1))
          is Deferred -> listOf(Deferred(Seq(listOf(it.parser) + seqTail.drop(1)), it.remainder))
        }
      }
    }

  override fun invoke(s: String) = internal(s, seq)
  override fun toString() = seq.joinToString(" \u22B3 ")
}

data class Alt(val alts: List<Seq>) : Parser {
  override fun invoke(s: String): List<Result> = alts.flatMap { it(s) }
  override fun toString() = alts.joinToString(" | ")
}
