package parser

import graph.LN

fun interface Parser {
  operator fun invoke(s: String): List<Result>
}

data class Term(val term: String) : Parser {
  override fun invoke(s: String) =
    if (s.startsWith(term)) listOf(Immediate(LN(this.toString()), s.removePrefix(term))) else listOf()

  override fun toString() = "'$term'"
}

data object Eps : Parser {
  override fun invoke(s: String) = listOf(Immediate(LN(this.toString()), s))
  override fun toString() = "\u03B5"
}

data class Def(val parser: Parser) : Parser {
  override fun invoke(s: String) = listOf(Deferred(LN(this.toString()), parser, s))
  override fun toString() = "[$parser]"
}

data class Seq(val p: Parser, val q: Parser) : Parser {
  override fun invoke(s: String): List<Result> {
    return p(s).flatMap { pr ->
      when (pr) {
        is Immediate -> q(pr.remainder).map { qr -> qr.fmap { LN("Seq", mutableListOf(pr.data.cp(), it)) } }
        is Deferred -> listOf(Deferred(LN("Seq", mutableListOf(pr.data.cp(), LN(q.toString()))), Seq(pr.parser, q), pr.remainder))
//          listOf(Seq(pr.parser, q).let { Deferred(LN(it), it, pr.remainder) })
      }
    }
  }

  override fun toString() = "$p \u22B3 $q"
}

data class Alt(val p: Parser, val q: Parser) : Parser {
  override fun invoke(s: String): List<Result> = p(s) + q(s)
  override fun toString() = "$p | $q"
}
