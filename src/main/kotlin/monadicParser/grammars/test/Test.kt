package monadicParser.grammars.test

import monadicParser.*

object E : BaseParser<String, State<CharSequence>>() {
  override val par: ParserM<String, State<CharSequence>> =
    def(
      (E bind { e -> term("e").map { e + it.toString() } })
              alt
              term("f").map { it.toString() }
    )
}

object F : BaseParser<String, State<CharSequence>>() {
  override val par: ParserM<String, State<CharSequence>> =
    def(
      (term("(") bind E bind { e -> term(")").map { e } }) alt E
    )
}

fun main() {
  println(run(F, State.ret("(feee)")))
//  println(run(E, State.ret("fee")))
}
