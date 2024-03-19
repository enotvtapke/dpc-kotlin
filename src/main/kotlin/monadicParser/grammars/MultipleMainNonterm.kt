package monadicParser.grammars

import monadicParser.*

object MultipleMainNonterm {

  data object S : BaseParser<CharSequence, State<CharSequence>>() {
    override val par: ParserM<CharSequence, State<CharSequence>> = def(C alt B)
  }

  data object C : BaseParser<CharSequence, State<CharSequence>>() {
    override val par: ParserM<CharSequence, State<CharSequence>> = def(
      B bind { cc -> term("c").map { c -> (cc.toString() + c.toString()) as CharSequence } } alt term("C")
    )
  }

  data object B : BaseParser<CharSequence, State<CharSequence>>() {
    override val par: ParserM<CharSequence, State<CharSequence>> = def(A bind { cc -> term("b").map { c -> (cc.toString() + c.toString()) as CharSequence } })
  }

  data object A : BaseParser<CharSequence, State<CharSequence>>() {
    override val par: ParserM<CharSequence, State<CharSequence>> = def(C bind { cc -> term("a").map { c -> (cc.toString() + c.toString()) as CharSequence } })
  }
}

fun main() {
  println(MultipleMainNonterm.S(State.ret("Cabcabc")).map { (it as Immediate<String, State<CharSequence>>).res})
}
