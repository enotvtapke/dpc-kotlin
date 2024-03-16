package monadicParser.grammars

import monadicParser.*

object Exponential {
  object E : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (E bind { e -> term("e").map { e + it.toString() } })
                alt
                F
      )
  }

  object F : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (term("(") bind E bind { e -> term(")").map { "($e)" } }) alt term("f") as ParserM<String, State<CharSequence>>
      )
  }
}

fun main() {
  println(Exponential.F(State.ret("(((((((((((((f)))))))))))))")))
}

