package monadicParserRefac.stack

import monadicParserRefac.*
import org.assertj.core.api.Assertions.assertThat

private fun <T, S> getResultsInternal(p: ParserM<T, State<S>>, s: State<S>) =
  p(s).also {
    assertThat(it.filterDef()).isEmpty()
  }.filterImm()

fun <T, S> getResults(p: ParserM<T, State<S>>, s: State<S>): List<T> = getResultsInternal(p, s).map { it.first.v }

fun <T, S> assertFail(p: ParserM<T, State<S>>, s: State<S>) {
  val r = getResults(p, s)
  assertThat(r).isEmpty()
}

fun <T, S> checkParser(p: ParserM<T, State<S>>, s: State<S>, expected: String) {
  val r = getResults(p, s)
  assertThat(r).hasSize(1)
  assertThat(r.first().toString()).isEqualTo(expected)
}

fun <T> checkParser(p: ParserM<T, State<String>>, inOut: String) {
  val r = getFullResults(p, inOut)
  assertThat(r).hasSize(1)
  assertThat(r.first().toString()).isEqualTo(inOut)
}

fun <T> getFullResults(
  p: ParserM<T, State<String>>,
  s: String
) = getResultsInternal(p, State.ret(s)).filter { it.second.s.isEmpty() }.map { it.first.v }
