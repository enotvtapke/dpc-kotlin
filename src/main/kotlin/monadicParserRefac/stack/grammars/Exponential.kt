package monadicParserRefac.stack.grammars

import monadicParserRefac.BaseParser
import monadicParserRefac.ParserM
import monadicParserRefac.stack.State
import monadicParserRefac.stack.def
import monadicParserRefac.stack.term

object Exponential {
  object E : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (E bind { e -> term("e").map { e + it } })
                alt
                F
      )
  }

  object F : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (term("(") bind E bind { e -> term(")").map { "($e)" } }) alt term("f")
      )
  }
}

fun main() {
  println(Exponential.F(State.ret("(((((((((((((f)))))))))))))")))
}

