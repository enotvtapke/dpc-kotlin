package parserRefac

val failedBranchesSet = FailedBranchesSet()

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

//data class Seq(val p: Parser, val q: Parser) : Parser {
//  override fun invoke(s: String): List<kotlin.Result> {
//    return p(s).flatMap { pr ->
//      when (pr) {
//        is Immediate -> q(pr.state)
//        is Deferred -> listOf(
//          Deferred(if (pr.parser is Seq) Seq(pr.parser.p, Seq(pr.parser.q, q)) else Seq(pr.parser, q), pr.state)
//        )
//      }
//    }
//  }
//
//  override fun toString() = "$p \u22B3 $q"
//}

data class Seq(val seq: List<Parser>) : Parser {
  private fun internal(s: String, consumed: List<Token>, seq: List<Parser>): List<Result> = when {
    seq.isEmpty() -> listOf(Immediate(consumed, s))
    else -> {
      val head = seq.first()
      val tail = seq.drop(1)
      head(s).flatMap { res ->
        when (res) {
          is Immediate -> internal(res.remainder, consumed + res.consumed, tail)
          is Failed -> {
            failedBranchesSet.add(s, consumed + res.consumed)
            listOf(Failed(consumed + res.consumed, res.remainder))
          }
          is Deferred -> {
            val r = if (res.parser is Seq) res.parser.seq else listOf(res.parser)
            listOf(Deferred(consumed + res.consumed, Seq(r + tail), res.remainder))
          }
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
