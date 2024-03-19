package monadicParser.stack.grammars

import monadicParser.BaseParserM
import monadicParser.ParserM
import monadicParser.stack.State
import monadicParser.stack.def
import monadicParser.stack.term

object VeryIndirect {

  data object C : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(
      B bind { cc -> term("c").map { c -> cc + c } } alt term("a")
    )
  }

  data object B : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(A)
  }

  data object A : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(C)
  }

}

fun main() {
  println(VeryIndirect.C(State.ret("acccc")))
}
