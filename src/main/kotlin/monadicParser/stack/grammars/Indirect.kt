package monadicParser.stack.grammars

import monadicParser.BaseParserM
import monadicParser.ParserM
import monadicParser.stack.State
import monadicParser.stack.def
import monadicParser.stack.term


object Indirect {
  data object C : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(
      B bind { cc -> term("c").map { c -> (cc.toString() + c) } } alt term("a")
    )
  }

  data object B : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> = def(C)
  }

}

object ComplexIndirectNotLLk {
  data object A : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (B bind { b -> term("a").map { b + it } })
                alt
                (A bind { b -> term("ba").map { b + it } })
      )
  }

  data object B : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (C bind { c -> term("b").map { c + it } })
      )
  }

  data object C : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        A alt term("c")
      )
  }
}

object ComplexIndirect {
  data object A : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (B bind { b -> term("a").map { b + it } })
                alt
                (term("A").map { it })
      )
  }

  data object B : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (C bind { c -> term("b").map { c + it } }) alt (term("B").map { it }) alt D
      )
  }

  data object C : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (A bind { b -> term("c").map { b + it } }) alt term("C").map { it }
      )
  }

  data object D : BaseParserM<String, State<String>>() {
    override val par: ParserM<String, State<String>> =
      def(
        (E bind { b -> term("d").map { b + it } })
                alt
                (term("D").map { it })
      )
  }

  data object E : BaseParserM<String, State<String>>() {
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
