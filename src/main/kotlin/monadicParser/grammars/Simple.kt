package monadicParser.grammars

import monadicParser.*
import java.lang.StringBuilder

object C : BaseParser<CharSequence, State<CharSequence>>() {
  override val par: ParserM<CharSequence, State<CharSequence>> = def(
    C bind { cc -> term("c").map { c -> (cc.toString() + c.toString()) as CharSequence } } alt term("a")
  )
}

fun main() {
  println(run(C, State.ret("ac")))
}
