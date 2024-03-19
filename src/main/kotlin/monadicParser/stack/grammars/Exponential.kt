package monadicParser.stack.grammars

import monadicParser.BaseParserM
import monadicParser.ParserM
import monadicParser.stack.State
import monadicParser.stack.counter
import monadicParser.stack.def
import monadicParser.stack.term

object Exponential {
  data object E : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (E bind { e -> term("e").map { e + it } })
                alt
                F
      )
  }

  data object F : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (term("(") bind E bind { e -> term(")").map { "($e)" } }) alt term("f")
      )
  }
}

private fun gen(n: Int) = "(".repeat(n) + "f" + ")".repeat(n)

fun main() {
  for (i in 0..100) {
    counter = 0
    println(Exponential.F(State(gen(i))))
    println("$i $counter")
  }

}

