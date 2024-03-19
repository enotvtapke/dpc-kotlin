package monadicParser.classic.grammars

import monadicParser.BaseParserM
import monadicParser.ParserM
import monadicParser.classic.*

object Exponential {
  object E : BaseParserM<String, String>() {
    override val par: ParserM<String, String> =
      def(
        (E bind { e -> term("e").map { e + it } })
                alt
                F
      )
  }

  object F : BaseParserM<String, String>() {
    override val par: ParserM<String, String> =
      def(
        (term("(") bind E bind { e -> term(")").map { "($e)" } }) alt term("f")
      )
  }
}

fun main() {
  println(runClassic(Exponential.F, "((((((((((((((f))))))))))))))", ::stopCriteria))
}
