package monadicParser.grammars

import monadicParser.*

object Indirect {
  data object C : BaseParser<CharSequence, State<CharSequence>>() {
    override val par: ParserM<CharSequence, State<CharSequence>> = def(
      B bind { cc -> term("c").map { c -> (cc.toString() + c.toString()) as CharSequence } } alt term("a")
    )
  }

  data object B : BaseParser<CharSequence, State<CharSequence>>() {
    override val par: ParserM<CharSequence, State<CharSequence>> = def(C)
  }

}

object ComplexIndirectNotLLk {
  data object A : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (B bind { b -> term("a").map { b + it.toString() } })
                alt
                (A bind { b -> term("ba").map { b + it.toString() } })
      )
  }

  data object B : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (C bind { c -> term("b").map { c + it.toString() } })
      )
  }

  data object C : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        A alt term("c") as ParserM<String, State<CharSequence>>
      )
  }
}

object ComplexIndirect {
  data object A : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (B bind { b -> term("a").map { b + it.toString() } })
                alt
                (term("A").map { it.toString() })
      )
  }

  data object B : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (C bind { c -> term("b").map { c + it.toString() } }) alt (term("B").map { it.toString() }) alt D
      )
  }

  data object C : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (A bind { b -> term("c").map { b + it.toString() } }) alt term("C").map { it.toString() }
      )
  }

  data object D : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (E bind { b -> term("d").map { b + it.toString() } })
                alt
                (term("D").map { it.toString() })
      )
  }

  data object E : BaseParser<String, State<CharSequence>>() {
    override val par: ParserM<String, State<CharSequence>> =
      def(
        (D bind { b -> term("e").map { b + it.toString() } })
                alt
                (term("E").map { it.toString() })
      )
  }
}


fun main() {
  println(Indirect.C(State.ret("ac")))
//  println(open(ComplexIndirect1.A(State.ret("cbaba")).filterIsInstance<Deferred<String, State<CharSequence>>>()))
//  println(ComplexIndirect.A(State.ret("Ededa")))
//  println(ComplexIndirect1.A(State.ret("Acbacba")))
}
