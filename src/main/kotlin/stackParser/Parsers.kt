package stackParser

fun interface Parser {
  operator fun invoke(s: String): List<Result>
}

data class Term(val term: String) : Parser {
  override fun invoke(s: String) =
    if (s.startsWith(term)) listOf(Immediate(s.removePrefix(term))) else listOf()

  override fun toString() = "'$term'"
}

data class Def(val parser: Parser) : Parser {
  override fun invoke(s: String) = listOf(Deferred(parser, s))
  override fun toString() = "[$parser]"
}

data class Seq(val p: Parser, val q: Parser) : Parser {
  override fun invoke(s: String): List<Result> {
    return p(s).flatMap { pr ->
      when (pr) {
        is Immediate -> q(pr.state)
        is Deferred -> listOf(
          Deferred(if (pr.parser is Seq) Seq(pr.parser.p, Seq(pr.parser.q, q)) else Seq(pr.parser, q), pr.state)
        )
      }
    }
  }

  override fun toString() = "$p \u22B3 $q"
}

data class Alt(val p: Parser, val q: Parser) : Parser {
  override fun invoke(s: String): List<Result> = p(s) + q(s)
  override fun toString() = "$p | $q"
}
