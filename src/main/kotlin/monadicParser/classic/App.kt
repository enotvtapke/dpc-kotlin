package monadicParser.classic

import monadicParser.ParserM
import monadicParser.Res
import monadicParser.classic.grammars.Lama.Expr
import monadicParser.classic.grammars.Lama.test
import monadicParser.filterDef

fun <T, S> runClassic(p: ParserM<T, S>, initialState: S, stopCriteria: (Res<T, S>) -> Boolean): Res<T, S> {
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
    listOf(runClassic(Expr, it, ::stopCriteria))
  }
//  val r = run(Expr, "1+1", ::stopCriteria)
//  val r = run(C, "accc", ::stopCriteria)
//  println(r)
}