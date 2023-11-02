package parser

private operator fun String.not() = Term(this)
operator fun Parser.times(b: Parser) = Seq(this, b)
operator fun Parser.plus(b: Parser) = Alt(this, b)
operator fun Parser.unaryPlus() = Def(this)

fun fix(l: String = "self", p: (Parser) -> (Parser)) = object : Parser {
  override fun invoke(s: String) = p(this)(s)
  override fun toString() = l
}

fun step(r: List<Result>) = r.flatMap {
  when (it) {
    is Deferred -> it.parser(it.remainder)
    is Immediate -> listOf(it)
  }
}

fun pass(p: Parser, s: String): Immediate {
  var res = p(s)
  var step = 1
  println("1 $res")
  while (res.filterIsInstance<Immediate>().all { it.remainder.isNotEmpty() }) {
    res = step(res)
    step++
    println("$step $res")
  }
  return res.filterIsInstance<Immediate>().first { it.remainder.isEmpty() }
}

fun main() {
  val ccc = fix("C") {
    +it * !"c" + !"a"
  }
  val term = fix("T") {
    +it * !"+" * it +
            +it * !"-" * it +
            !"a"
  }
  println(pass(ccc, "accc"))
  println(pass(term, "a+a-a"))
}

private data object C : Parser {
  override fun invoke(s: String) = (+this * !"c" + Eps)(s)
}
