package parserRefac

import graph.Node
import graph.renderGraph
import java.util.function.Predicate

private data class ResultWrapper(val node: Node, val result: Result)

private operator fun Char.not() = Term(this)

private operator fun Parser.times(b: Parser) = Seq(listOf(this, b))
private operator fun Seq.times(b: Parser) = Seq(this.seq + listOf(b))
private operator fun Seq.plus(b: Seq) = Alt(listOf(this, b))

private operator fun Seq.plus(b: Parser) = Alt(listOf(this, Seq(listOf(b))))
private operator fun Alt.plus(b: Seq) = Alt(this.alts + listOf(b))

private operator fun Alt.plus(b: Parser) = Alt(this.alts + listOf(Seq(listOf(b))))
private operator fun Parser.unaryMinus() = Def(this)

private fun step(r: List<ResultWrapper>) = r.flatMap { (prevNode, prevRes) ->
  when (prevRes) {
    is Deferred -> {
      prevRes.parser(prevRes.remainder).map { curRes ->
        val curNode = Node(curRes.toString())
        prevNode.addChild(curNode)
        ResultWrapper(curNode, curRes)
      }
    }

    is Immediate, is Failed -> {
      listOf(ResultWrapper(prevNode, prevRes))
    }
  }
}

fun Parser.run(
  s: String,
  maxStep: Int = Int.MAX_VALUE,
  stopCriteria: Predicate<Result> = Predicate { it is Immediate && it.remainder.isEmpty() },
  enableLogging: Boolean = true
): Result? {
  val root = Node(this.toString())
  var stepRes = listOf(ResultWrapper(root, Deferred(listOf(), this, s)))
  var stepNumber = 1
  var parseResult: Result? = null
  run br@ {
    repeat(maxStep) {
      val stepResResult = stepRes.map { it.result }
      if (enableLogging) println("$stepNumber $stepResResult")
      if (stepResResult.any(stopCriteria::test)) {
        parseResult = stepResResult.first(stopCriteria::test)
        return@br
      }
      stepRes = step(stepRes)
      stepNumber++
    }
  }
  if (enableLogging) renderGraph("parserRefac_res", root.toString())
  return parseResult
}

fun main() {
  failedBranchesSet.clear()
  println("\nResult of applying $CCC:\n" + CCC.run("ccccacccc", 14, enableLogging = true))
//  println("\nResult for $CCC:\n" + CCC.run("ccca", 20))
  println("\nSet of failed branches (size ${failedBranchesSet.size()}):")
  println(failedBranchesSet)
}

private data object CCC : Parser {
  override fun invoke(s: String) = (-this * !'c' + !'c')(s)
}

private data object T : Parser {
  override fun invoke(s: String) = (-T * !'+' * !'a' + -T * !'-' * !'a' + !'a')(s)
}

