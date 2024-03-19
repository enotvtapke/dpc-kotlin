package monadicParserRefac.stack.grammars

import monadicParserRefac.BaseParser
import monadicParserRefac.ParserM
import monadicParserRefac.stack.State
import monadicParserRefac.stack.def
import monadicParserRefac.stack.term


object C : BaseParser<String, State<String>>() {
  override val par: ParserM<String, State<String>> = def(
    C bind { cc -> term("c").map { c -> cc + c } } alt term("a")
  )
}

fun main() {
  println(C(State("ac")))
}
