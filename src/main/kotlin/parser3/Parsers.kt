package parser3

fun interface Parser {
  operator fun invoke(s: String): List<Result>
}

abstract class NonTerminal(val label: String) : Parser {
  private fun step(r: List<Result>): List<Result> = r.flatMap {
    when (it) {
      is Deferred -> if (it.isLeftRec()) listOf(it) else step(it.parser(it.remainder))
      is Immediate -> listOf(it)
    }
  }

  abstract fun checkedInvoke(s: String): List<Result>

  override fun invoke(s: String): List<Result> {
    val res = step(checkedInvoke(s))
    return if (res.all { it.isLeftRec() }) listOf() else res
  }

  private fun Result.isLeftRec() =
    this is Deferred && (this.parser == this || this.parser is Seq && this.parser.seq.first() == this@NonTerminal)

  override fun toString() = label
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

// То, что сейчас сделано в NonTerminal должно быть сделано в Seq. То есть Seq должен распознавать левую рекурсию
data class Seq(val seq: List<Parser>, val parentNonTerm: Parser) : Parser {
  private fun internal(s: String, seqTail: List<Parser>): List<Result> =
    when {
      seqTail.isEmpty() -> listOf(Immediate(s))
      else -> seqTail.first()(s).flatMap {
        when (it) {
          is Immediate -> internal(it.remainder, seqTail.drop(1))
          is Deferred -> listOf(Deferred(Seq(listOf(it.parser) + seqTail.drop(1)), it.remainder)) // здесь должен быть isLeftReq if и flatmap
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
