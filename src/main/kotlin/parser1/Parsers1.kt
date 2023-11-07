package parser1

import graph.Node1

fun interface Parser {
  operator fun invoke(s: String): List<Node1>
}

data class Term(val term: String) : Parser {
  override fun invoke(s: String) = if (s.startsWith(term)) listOf(Node1(Immediate(s.removePrefix(term)))) else listOf()

  override fun toString() = "'$term'"
}

data object Eps : Parser {
  override fun invoke(s: String) = listOf(Node1(Immediate(s)))
  override fun toString() = "\u03B5"
}

data class Def(val parser: Parser) : Parser {
  override fun invoke(s: String) = listOf(Node1(Deferred(parser, s)))
}

data class Seq(val p: Parser, val q: Parser) : Parser {
  override fun invoke(s: String) = p(s).flatMap {
    val r = it.label as Result
    when (r) {
      is Immediate -> q(r.remainder).onEach { qNode -> qNode.setParent(it) }
      is Deferred -> listOf(Node1(Deferred(Seq(r.parser, q), r.remainder)))
    }
  }

  override fun toString() = "$p \u22B3 $q"
}

data class Alt(val p: Parser, val q: Parser) : Parser {
  override fun invoke(s: String) = p(s) + q(s)
  override fun toString() = "$p | $q"
}
