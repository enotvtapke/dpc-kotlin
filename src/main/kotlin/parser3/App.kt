package parser3

import graph.Node
import java.io.File

private data class ResultWrapper(val node: Node, val result: Result)

private operator fun String.not() = Term(this)
operator fun Parser.times(b: Parser) = Seq(listOf(this, b))
operator fun Seq.times(b: Parser) = Seq(this.seqs + listOf(b))
operator fun Seq.plus(b: Seq) = Alt(listOf(this, b))
operator fun Seq.plus(b: Parser) = Alt(listOf(this, Seq(listOf(b))))
operator fun Alt.plus(b: Seq) = Alt(this.alts + listOf(b))
operator fun Alt.plus(b: Parser) = Alt(this.alts + listOf(Seq(listOf(b))))
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
  File("./res1_parser3.dot").writeText(root.toString())
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
  println(ccc)
  println(pass(T, "a+a-a", 5))
//  println(pass(term, "a+a-a"))
}

private data object C : Parser {
  override fun invoke(s: String) = (-this * !"c" + !"c")(s)
}

private data object T : Parser {
  override fun invoke(s: String) = (-T * !"+" * !"a" + -T * !"-" * !"a" + !"a")(s)
}

private data object A : Parser {
  override fun invoke(s: String) = (!"a")(s)
}
