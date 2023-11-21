package parser3

import graph.Node
import graph.renderGraph

private data class ResultWrapper(val node: Node, val result: Result)

private operator fun String.not() = Term(this)

//operator fun Parser.times(b: Parser) = Seq(listOf(this, b))
//operator fun Seq.times(b: Parser) = Seq(this.seq + listOf(b))
operator fun Seq.plus(b: Seq) = Alt(listOf(this, b))

//operator fun Seq.plus(b: Parser) = Alt(listOf(this, Seq(listOf(b))))
operator fun Alt.plus(b: Seq) = Alt(this.alts + listOf(b))

//operator fun Alt.plus(b: Parser) = Alt(this.alts + listOf(Seq(listOf(b))))
operator fun Parser.unaryMinus() = Def(this)

fun fix(l: String = "self", p: (Parser) -> (Parser)) = object : Parser {
  override fun invoke(s: String) = p(this)(s)
  override fun toString() = l
}

private fun step(r: List<ResultWrapper>) = r.flatMap { (prevNode, prevRes) ->
  when (prevRes) {
    is Deferred -> {
      val res = prevRes.parser(prevRes.remainder).map { curRes ->
        val curNode = Node(curRes.toString())
        prevNode.addChild(curNode)
        ResultWrapper(curNode, curRes)
      }
      if (res.all {
          it.result is Deferred && it.result.parser is Seq && prevRes.parser is Seq && it.result.parser.seq.first() == prevRes.parser.seq.first()
        }) listOf() else res
    }

    is Immediate -> {
      listOf(ResultWrapper(prevNode, prevRes))
    }
  }
}

private fun List<ResultWrapper>.filterImmediate() =
  this.filter { it.result is Immediate }.map { it.result as Immediate }

fun pass(p: Parser, s: String, maxStep: Int = Int.MAX_VALUE): Immediate {
  val root = Node("root")
  var res = listOf(ResultWrapper(root, Deferred(p, s)))
  var step = 1
  while (res.filterImmediate().all { it.remainder.isNotEmpty() } && res.any { it.result is Deferred }) {
    println("$step ${res.map { it.result }}")
    res = step(res)
    step++
    if (step == maxStep) break
  }
  println("$step ${res.map { it.result }}")
  renderGraph("parser3_res", root.toString())
  return if (maxStep != Int.MAX_VALUE) {
    Immediate("a")
  } else {
    res.filterImmediate().first { it.remainder.isEmpty() }
  }
}

fun main() {
//  val ccc = fix("C") {
//    -it * !"a" +
//    !"c"
//  }
//  val term = fix("T") {
//    -it * !"+" * it +
//    -it * !"-" * it +
//    !"a"
//  }

//  println(ccc)
  pass(T, "a+a-a", 5)

//  pass(CC, "", 10)

//  println(pass(term, "a+a-a"))
}

//private data object C : Parser {
//  override fun invoke(s: String) = (-this * !"c" + !"c")(s)
//}

private data object CC : NonTerminal("CC") {
  override fun checkedInvoke(s: String) = (Seq(this, listOf(-this, !"c")) + Seq(this, listOf(!"c")))(s)
}

private data object T : Parser {
  override fun invoke(s: String) =
    (
            Seq(this, listOf(-T, !"+", !"a"))
                    +
                    Seq(this, listOf(-T, !"-", !"a"))
                    +
                    Seq(this, listOf(!"a"))
            )(s)
}

//
//private data object A : Parser {
//  override fun invoke(s: String) = (!"a")(s)
//}
