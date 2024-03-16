package parserRefac.grammars

import parserRefac.*
import parserRefac.Result

private operator fun Char.not() = Term(this)

private operator fun Parser.times(b: Parser) = Seq(listOf(this, b))
private operator fun Seq.times(b: Parser) = Seq(this.seq + listOf(b))
private operator fun Seq.plus(b: Seq) = Alt(listOf(this, b))

private operator fun Seq.plus(b: Parser) = Alt(listOf(this, Seq(listOf(b))))
private operator fun Alt.plus(b: Seq) = Alt(this.alts + listOf(b))

private operator fun Alt.plus(b: Parser) = Alt(this.alts + listOf(Seq(listOf(b))))
private operator fun Parser.unaryMinus() = Def(this)


data object E : Parser {
  override fun invoke(s: String): List<Result> = (-E * !'+' * T + T)(s)
}

data object T : Parser {
  override fun invoke(s: String): List<Result> = (-T * !'*' * F + F)(s)
}

data object F : Parser {
  override fun invoke(s: String): List<Result> = (!'(' * E * !')'  + !'1')(s)
}


