package parser2

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

data class Seq(val p: Parser, val q: Parser) : Parser {
  override fun invoke(s: String) = p(s).flatMap {
    when (it) {
      is Immediate -> q(it.remainder)
      is Deferred -> listOf(Deferred(Seq(it.parser, q), it.remainder))
    }
  }
  override fun toString() = "$p \u22B3 $q"
}

data class Alt(val p: Parser, val q: Parser) : Parser {
  override fun invoke(s: String): List<Result> = p(s) + q(s)
  override fun toString() = "$p | $q"
}
