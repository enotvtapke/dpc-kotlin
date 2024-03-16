package monadicParser.grammars

import monadicParser.*

object Brackets {
  object E : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (E bind { e -> term("e").map { e + it.toString() } })
                alt
                term("f").map { it.toString() }
      )
  }

  object F : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (term("(") bind E bind { e -> term(")").map { "($e)" } }) alt E
      )
  }
}

fun main() {
  println(Brackets.F(State.ret("(feeee)")))
}
