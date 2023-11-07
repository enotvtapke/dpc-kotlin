package parser

import java.io.File

private operator fun String.not() = Term(this)
operator fun Parser.times(b: Parser) = Seq(this, b)
operator fun Parser.plus(b: Parser) = Alt(this, b)
operator fun Parser.unaryMinus() = Def(this)

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
  res.toDot("res_${step}.dot")
  while (res.filterIsInstance<Immediate>().all { it.remainder.isNotEmpty() }) {
    res = step(res)
    step++
    println("$step $res")
//    File("./res.dot").writeText(res.joinToString("\n\n") { it.data().toString() })
    res.toDot("res_${step}.dot")
  }
  return res.filterIsInstance<Immediate>().first { it.remainder.isEmpty() }
}

private fun List<Result>.toDot(filename: String = "res.dot") {
  File("./$filename").writeText(
    buildString {
      append("graph G {")
      appendLine()
      append(this@toDot.joinToString("\n") { it.data().toString().replace("graph", "subgraph") }.prependIndent("  "))
      appendLine()
      append("}")
      appendLine()
    })
}

fun main() {
  val ccc = fix("C") {
    -it * !"c" + !"a"
  }
  val term = fix("T") {
    -it * !"+" * it +
            -it * !"-" * it +
            !"a"
  }
//  println(pass(ccc, "acccc"))
  println(pass(term, "a+a-a"))
}

private data object C : Parser {
  override fun invoke(s: String) = (-this * !"c" + Eps)(s)
}

private data object B : Parser {
  override fun invoke(s: String) = (C * !"b")(s)
}
