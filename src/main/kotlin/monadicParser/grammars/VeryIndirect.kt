package monadicParser.grammars

import monadicParser.*

object VeryIndirect {

  data object C : BaseParser<CharSequence, State<CharSequence>>() {
    override val par: ParserM<CharSequence, State<CharSequence>> = def(
      B bind { cc -> term("c").map { c -> (cc.toString() + c.toString()) as CharSequence } } alt term("a")
    )
  }

  data object B : BaseParser<CharSequence, State<CharSequence>>() {
    override val par: ParserM<CharSequence, State<CharSequence>> = def(A)
  }

  data object A : BaseParser<CharSequence, State<CharSequence>>() {
    override val par: ParserM<CharSequence, State<CharSequence>> = def(C)
  }

}

fun main() {
  println(VeryIndirect.C(State.ret("acccc")))
}
