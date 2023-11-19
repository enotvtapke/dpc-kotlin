package parser

import graph.Node
import graph.renderGraph
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

fun pass(p: Parser, s: String, maxStep: Int = Int.MAX_VALUE): Immediate {
  var res = p(s)
  var step = 1
  println("1 $res")
  renderGraph("parser_res_${step}", res.toDot())
  while (res.filterIsInstance<Immediate>().all { it.remainder.isNotEmpty() }) {
    res = step(res)
    step++
    println("$step $res")
    renderGraph("parser_res_${step}", res.toDot())
    if (step == maxStep) {
      break
    }
  }
  return if (maxStep != Int.MAX_VALUE) {
    Immediate(Node("a"), "a")
  } else {
    res.filterIsInstance<Immediate>().first { it.remainder.isEmpty() }
  }
}

private fun List<Result>.toDot() = buildString {
    append("graph G {")
    appendLine()
    append(this@toDot.joinToString("\n") { it.data().toString().replace("graph", "subgraph") }.prependIndent("  "))
    appendLine()
    append("}")
    appendLine()
  }

fun main() {
  val ccc = fix("C") {
    -it * !"a" + !"c"
  }
  val term = fix("T") {
    -it * !"+" * it +
    -it * !"-" * it +
    !"a"
  }
  println(pass(ccc, "ccc", maxStep = 5))
//  println(pass(term, "a+a-a"))
}

private data object C : Parser {
  override fun invoke(s: String) = (-this * !"c" + Eps)(s)
}

private data object B : Parser {
  override fun invoke(s: String) = (C * !"b")(s)
}
