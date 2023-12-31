package parser2

import graph.Node
import graph.renderGraph
import java.io.File

private data class ResultWrapper(val node: Node, val result: Result)

private operator fun String.not() = Term(this)
operator fun Parser.times(b: Parser) = Seq(this, b)
operator fun Parser.plus(b: Parser) = Alt(this, b)
operator fun Parser.unaryMinus() = Def(this)

fun fix(l: String = "self", p: (Parser) -> (Parser)) = object : Parser {
  override fun invoke(s: String) = p(this)(s)
  override fun toString() = l
}

private fun step(r: List<ResultWrapper>) = r.flatMap { (prevNode, prevRes) ->
  when (prevRes) {
    is Deferred -> {
      prevRes.parser(prevRes.remainder).map { curRes ->
        val curNode = Node(curRes.toString())
        prevNode.addChild(curNode)
        ResultWrapper(curNode, curRes)
      }
    }
    is Immediate -> {
      listOf(ResultWrapper(prevNode, prevRes))
    }
  }
}

private fun List<ResultWrapper>.filterImmediate() = this.filter { it.result is Immediate }.map { it.result as Immediate }

fun pass(p: Parser, s: String, maxStep: Int = Int.MAX_VALUE): Immediate {
  val root = Node("root")
  var res = listOf(ResultWrapper(root, Deferred(p, s)))
  var step = 1
  println("1 $res")
  while (res.filterImmediate().all { it.remainder.isNotEmpty() }) {
    res = step(res)
    step++
    println("$step ${res.map { it.result }}")
    if (step == maxStep) break
  }
  renderGraph("parser2_res", root.toString())
//  File("./res1.dot").writeText(root.toString())
  return if (maxStep != Int.MAX_VALUE) {
    Immediate("a")
  } else {
    res.filterImmediate().first { it.remainder.isEmpty() }
  }
}

fun main() {
  val ccc = fix("C") {
    -it * !"a" +
    !"c"
  }
//  val term = fix("T") {
//    -it * !"+" * it +
//    -it * !"-" * it +
//    !"a"
//  }
  println(pass(ccc, "ccc", 5))
//  println(pass(term, "a+a-a"))
}

private data object C : Parser {
  override fun invoke(s: String) = (-this * !"c" + Eps)(s)
}

private data object B : Parser {
  override fun invoke(s: String) = (C * !"b")(s)
}
