package monadicParser.stack.grammars

import monadicParser.BaseParserM
import monadicParser.ParserM
import monadicParser.stack.State
import monadicParser.stack.def
import monadicParser.stack.term


object C : BaseParserM<String, State<String>>() {
  override val par: ParserM<String, State<String>> = def(
    C bind { cc -> term("c").map { c -> cc + c } } alt term("a")
  )
}

fun main() {
  println(C(State("ac")))
}
