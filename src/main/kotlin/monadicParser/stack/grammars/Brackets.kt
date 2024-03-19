package monadicParser.stack.grammars

import monadicParser.*
import monadicParser.stack.State
import monadicParser.stack.def
import monadicParser.stack.term

object Brackets {
  data object E : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (E bind { e -> term("e").map { e + it } })
                alt
                term("f").map { it }
      )
  }

  data object F : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (term("(") bind E bind { e -> term(")").map { "($e)" } }) alt E
      )
  }
}

fun main() {
  println(Brackets.F(State.ret("(feeeee)")))
}
