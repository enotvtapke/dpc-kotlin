package monadicParserRefac.stack.grammars

import monadicParserRefac.BaseParser
import monadicParserRefac.ParserM
import monadicParserRefac.stack.State
import monadicParserRefac.stack.def
import monadicParserRefac.stack.term


object Indirect {
  data object C : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(
      B bind { cc -> term("c").map { c -> (cc.toString() + c) } } alt term("a")
    )
  }

  data object B : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(C)
  }

}

object ComplexIndirectNotLLk {
  data object A : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (B bind { b -> term("a").map { b + it } })
                alt
                (A bind { b -> term("ba").map { b + it } })
      )
  }

  data object B : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (C bind { c -> term("b").map { c + it } })
      )
  }

  data object C : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        A alt term("c")
      )
  }
}

object ComplexIndirect {
  data object A : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (B bind { b -> term("a").map { b + it } })
                alt
                (term("A").map { it })
      )
  }

  data object B : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (C bind { c -> term("b").map { c + it } }) alt (term("B").map { it }) alt D
      )
  }

  data object C : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (A bind { b -> term("c").map { b + it } }) alt term("C").map { it }
      )
  }

  data object D : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (E bind { b -> term("d").map { b + it } })
                alt
                (term("D").map { it })
      )
  }

  data object E : BaseParser<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (D bind { b -> term("e").map { b + it } })
                alt
                (term("E").map { it })
      )
  }
}


fun main() {
  println(Indirect.C(State.ret("ac")))
//  println(open(ComplexIndirect1.A(State.ret("cbaba")).filterIsInstance<Deferred<String, State<CharSequence>>>()))
//  println(ComplexIndirect.A(State.ret("Ededa")))
//  println(ComplexIndirect1.A(State.ret("Acbacba")))
}
