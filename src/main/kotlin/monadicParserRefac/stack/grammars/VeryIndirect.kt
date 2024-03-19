package monadicParserRefac.stack.grammars

import monadicParserRefac.BaseParser
import monadicParserRefac.ParserM
import monadicParserRefac.stack.State
import monadicParserRefac.stack.def
import monadicParserRefac.stack.term

object VeryIndirect {

  data object C : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(
      B bind { cc -> term("c").map { c -> cc + c } } alt term("a")
    )
  }

  data object B : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(A)
  }

  data object A : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(C)
  }

}

fun main() {
  println(VeryIndirect.C(State.ret("acccc")))
}
