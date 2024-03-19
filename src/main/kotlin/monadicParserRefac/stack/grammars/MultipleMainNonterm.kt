package monadicParserRefac.stack.grammars

import monadicParserRefac.BaseParser
import monadicParserRefac.Imm
import monadicParserRefac.ParserM
import monadicParserRefac.stack.State
import monadicParserRefac.stack.def
import monadicParserRefac.stack.term


object MultipleMainNonterm {

  data object S : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(C alt B)
  }

  data object C : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(
      B bind { cc -> term("c").map { c -> cc + c } } alt term("C")
    )
  }

  data object B : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(A bind { cc -> term("b").map { c -> cc + c } })
  }

  data object A : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(C bind { cc -> term("a").map { c -> cc + c } })
  }
}

fun main() {
  println(MultipleMainNonterm.S(State.ret("Cabcabc")).map { (it.first as Imm<*, String>).v})
}
