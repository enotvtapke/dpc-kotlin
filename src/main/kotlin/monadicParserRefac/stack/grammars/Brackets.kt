package monadicParserRefac.stack.grammars

import monadicParserRefac.*
import monadicParserRefac.stack.State
import monadicParserRefac.stack.def
import monadicParserRefac.stack.term

object Brackets {
  data object E : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (E bind { e -> term("e").map { e + it } })
                alt
                term("f").map { it }
      )
  }

  data object F : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (term("(") bind E bind { e -> term(")").map { "($e)" } }) alt E
      )
  }
}

fun main() {
  println(Brackets.F(State.ret("(feeeee)")))
}