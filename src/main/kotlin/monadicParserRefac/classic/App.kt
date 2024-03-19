package monadicParserRefac.classic

import monadicParserRefac.ParserM
import monadicParserRefac.Res
import monadicParserRefac.classic.grammars.Lama.Expr
import monadicParserRefac.classic.grammars.Lama.test
import monadicParserRefac.filterDef

fun <T, S> run(p: ParserM<T, S>, initialState: S, stopCriteria: (Res<T, S>) -> Boolean): Res<T, S> {
  var r = p(initialState)
  while (true) {
    val finalR = r.find(stopCriteria)
    if (finalR != null) return finalR
    r = r.filterDef().flatMap { it.first.v(it.second) }
  }
}

fun <T> stopCriteria(r: Res<T, String>) = r.second.isEmpty()

fun main() {
  test(20) {
    listOf(run(Expr, it, ::stopCriteria))
  }
//  val r = run(Expr, "1+1", ::stopCriteria)
//  val r = run(C, "accc", ::stopCriteria)
//  println(r)
}