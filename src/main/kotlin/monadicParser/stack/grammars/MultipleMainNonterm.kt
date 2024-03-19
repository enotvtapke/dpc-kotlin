package monadicParser.stack.grammars

import monadicParser.BaseParserM
import monadicParser.Imm
import monadicParser.ParserM
import monadicParser.stack.State
import monadicParser.stack.def
import monadicParser.stack.term


object MultipleMainNonterm {

  data object S : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(C alt B)
  }

  data object C : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(
      B bind { cc -> term("c").map { c -> cc + c } } alt term("C")
    )
  }

  data object B : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(A bind { cc -> term("b").map { c -> cc + c } })
  }

  data object A : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(C bind { cc -> term("a").map { c -> cc + c } })
  }
}

fun main() {
  println(MultipleMainNonterm.S(State.ret("Cabcabc")).map { (it.first as Imm<*, String>).v})
}
