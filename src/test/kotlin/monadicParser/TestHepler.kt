package monadicParser

import org.assertj.core.api.Assertions.assertThat

private fun <T, S> getResultsInternal(p: ParserM<T, State<S>>, s: State<S>): List<Immediate<T, State<S>>> =
  p(s).also {
    assertThat(it.filterIsInstance<Deferred<*, *>>()).isEmpty()
  }.filterIsInstance<Immediate<T, State<S>>>()

fun <T, S> getResults(p: ParserM<T, State<S>>, s: State<S>): List<T> = getResultsInternal(p, s).map { it.res }

fun <T, S> assertFail(p: ParserM<T, State<S>>, s: State<S>) {
  val r = getResults(p, s)
  assertThat(r).isEmpty()
}

fun <T, S> checkParser(p: ParserM<T, State<S>>, s: State<S>, expected: String) {
  val r = getResults(p, s)
  assertThat(r).hasSize(1)
  assertThat(r.first().toString()).isEqualTo(expected)
}

fun <T> checkParser(p: ParserM<T, State<CharSequence>>, inOut: CharSequence) {
  val r = getResultsInternal(p, State.ret(inOut)).filter { it.state.s.isEmpty() }.map { it.res }
  assertThat(r).hasSize(1)
  assertThat(r.first().toString()).isEqualTo(inOut)
}