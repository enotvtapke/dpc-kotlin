package monadicParser.classic.grammars

import monadicParser.BaseParserM
import monadicParser.ParserM
import monadicParser.classic.def
import monadicParser.classic.grammars.Expr.E
import monadicParser.classic.runClassic
import monadicParser.classic.stopCriteria
import monadicParser.classic.term

object Expr {
  sealed interface Expr
  data class ExprOp(val left: Expr, val right: Term, val op: String) : Expr {
    override fun toString(): String {
//    return "Expr('$op', $left, $right)"
      return "$left$op$right"
    }
  }

  data class ExprVal(val term: Term) : Expr {
    override fun toString() = term.toString()
  }

  sealed interface Term
  data class TermOp(val left: Term, val right: FF, val op: String) : Term {
    override fun toString(): String {
//    return "Term('$op', $left, $right)"
      return "$left$op$right"
    }
  }

  data class TermVal(val ff: FF) : Term {
    override fun toString() = ff.toString()
  }

  sealed interface FF
  data class FFExpr(val expr: Expr) : FF {
    override fun toString() = "($expr)"
  }

  data class FFVal(val n: Int) : FF {
    override fun toString(): String {
      return "$n"
    }
  }

  data object E : BaseParserM<Expr, String>() {
    override val par: ParserM<Expr, String> =
      def(
        (E bind { e -> term("+") bind { op -> T.map { t -> ExprOp(e, t, op) as Expr } } })
                alt
                T.map { ExprVal(it) }
      )
  }

  data object T : BaseParserM<Term, String>() {
    override val par: ParserM<Term, String> =
      def(
        (T bind { t -> term("*") bind { op -> F.map { f -> TermOp(t, f, op.toString()) as Term } } })
                alt
                F.map { TermVal(it) }
      )
  }

  data object F : BaseParserM<FF, String>() {
    override val par: ParserM<FF, String> =
      def(
        (term("(") bind E bind { e -> term(")").map { FFExpr(e) as FF } })
                alt
                term("\\d+".toRegex()).map { FFVal(it.toString().toInt()) }
      )
  }
}

fun main() {
//  println(run(E, State.ret("(1+1+1+1)*3")).filter { (it as Immediate<Expr, State<CharSequence>>).state.s.isEmpty()})
  println(runClassic(E, "(((((((((42)))))))))", ::stopCriteria).first)
//  println(run(E, State.ret("42")))
//  println(E(State.ret("42")))
//  println(E(State.ret("(((((1)))))")))
}
