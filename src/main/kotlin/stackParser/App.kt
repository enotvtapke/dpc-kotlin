package stackParser

import stackParser.grammars.Indirect

private operator fun Parser.times(b: Parser) = Seq(this, b)

private fun fix(l: String = "self", p: (Parser) -> (Parser)) = object : Parser {
  override fun invoke(s: String) = p(this)(s)
  override fun toString() = l
}

abstract class BaseParser : Parser {
  private var r: Parser? = null
  abstract val par: Parser

  override fun invoke(s: String): List<Result> {
    if (r == null) r = par
    return r!!(s)
  }
}

private class Runner {
  private data class StackElement<T>(val parser: Parser, val state: T)

  private val stack: ArrayDeque<StackElement<String>> = ArrayDeque()

  private fun inStack(def: Deferred) =
    stack.any { (it.parser == def.parser || def.parser is Seq && (it.parser == def.parser.p || it.parser is Seq && it.parser.p == def.parser.p)) && it.state == def.state }

  private fun <T> inStack(el: StackElement<T>) = stack.any { it == el }
//    stack.any { (it.parser == def.parser || def.parser is Seq && (it.parser == def.parser.p || it.parser is Seq && it.parser.p == def.parser.p)) && it.state == def.state }

  private data class StepResult(
    val immediateResults: List<Immediate>,
    val leftRecursiveResults: List<Deferred>
  )

  /*
   Раскручивает парсер parser до конца, в результате step могут быть возвращены immediate и леворекурсивные результаты, относящиеся
   как к parser, так и ко всем парсерам, вызванным в его рамках. При это гарантируется, что все immediate результаты parser, которые парсер
   может породить непосредственно или опосредованно, будут возвращны из step. Таким образом мы точно не отсечём лишних веток
   и вместе с тем завершимся (когда список immediate пуст). При это из step могут быть возвращены леворекурсивные парсеры, которые обречены на бесконечную рекурсию
   и которые надо обрезать. К таким результатам могут относится как обречённые леворекурсивные результаты самого parser, тогда мы обрежем parser целиком,
   так и обречённые леворекурсивные результаты парсеров, являющихся потомками parser, эти леворекурсивные результаты внутри run для parser обрезаны не будут, они будут
   при дальнейших вызовах run на них. Иными словами важно понимать, что по результатам step можем быть максимально отрезана одна ветвь.

   Кстати стэк, пожалуй, можно представлять как автомат или путь в графе для облегчения понимания.
   */
  private fun step(parser: Parser, state: String): StepResult {
    stack.addLast(StackElement(parser, state))
    val results = parser(state)
    val (leftRecursiveResults, deferredResults) = results.filterIsInstance<Deferred>().partition(::inStack)
    val immediateResults = results.filterIsInstance<Immediate>()
    return if (deferredResults.isEmpty()) {
      stack.removeLast()
      StepResult(immediateResults = immediateResults, leftRecursiveResults = leftRecursiveResults)
    } else {
      val m = deferredResults.map { step(it.parser, it.state) }
      stack.removeLast()
      StepResult(
        immediateResults = immediateResults + m.flatMap { it.immediateResults },
        leftRecursiveResults = leftRecursiveResults + m.flatMap { it.leftRecursiveResults },
      )
    }
  }


//  private fun step(parser: Parser, state: String): StepResult {
//    if (inStack(StackElement(parser, state))) return StepResult(
//      immediateResults = listOf(),
//      leftRecursiveResults = listOf(Deferred(parser, state))
//    )
//    stack.addLast(StackElement(parser, state))
//    val results = parser(state)
//    val deferredResults = results.filterIsInstance<Deferred>()
////    val (leftRecursiveResults, deferredResults) = results.filterIsInstance<Deferred>().partition(::inStack)
//    val immediateResults = results.filterIsInstance<Immediate>()
//    val m = deferredResults.map { step(it.parser, it.state) }
//    stack.removeLast()
//    StepResult(
//      immediateResults = immediateResults + m.flatMap { it.immediateResults },
//      leftRecursiveResults = m.flatMap { it.leftRecursiveResults },
//    )
//  }

//  private fun step(parser: Parser, state: String): StepResult {
//    if (inStack(StackElement(parser, state))) return StepResult(
//      immediateResults = listOf(),
//      leftRecursiveResults = listOf(Deferred(parser, state))
//    )
//    stack.addLast(StackElement(parser, state))
//    val results = parser(state)
//    val leftRecursiveResults = results.filterIsInstance<Deferred>()
//    val immediateResults = results.filterIsInstance<Immediate>()
//    stack.removeLast()
//    return StepResult(immediateResults = immediateResults, leftRecursiveResults = leftRecursiveResults)
//  }

  fun run(parser: Parser, initialState: String): List<Result> {
    val stepResult = step(parser, initialState)
    return if (stepResult.leftRecursiveResults.isEmpty()) stepResult.immediateResults
    else if (stepResult.immediateResults.isEmpty()) listOf()
    else {
      stepResult.immediateResults + stepResult.leftRecursiveResults.flatMap { run(it.parser, it.state) }
    }
  }
}

private object A: BaseParser() {
  override val par: Parser
    get() = seq(-C, !"a") + !"a"
}

private object C: BaseParser() {
  override val par: Parser
    get() = seq(-C, !"d") + seq(-A, !"c")
}

private object CC: BaseParser() {
  override val par: Parser
    get() = Seq(-CC, !"d") + Seq(-CC, !"c") + !"e"
}

private object CCС: BaseParser() {
  override val par: Parser
    get() = (Seq(-CCС, !"a") + !"c")
}

fun main() {
  val ccc = fix("C") {
    (Seq(-it, !"a") + !"c")
  }
  val term = fix("T") {
    Seq(it, Seq(!"+", it)) +
      Seq(it, Seq(!"-", it)) +
      !"a"
  }
//  println(Runner().run(many(!"a"), "aaaa"))
//  println(Runner().run(ccc, "caaa"))
  println(Runner().run(Indirect.C, "acccc"))

//  println(Runner().run(CC, "ecccddd"))
}
