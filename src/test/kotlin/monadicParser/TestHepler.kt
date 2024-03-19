package monadicParser

import monadicParser.stack.State
import org.assertj.core.api.Assertions.assertThat

private fun <T, S> getResultsInternal(p: ParserM<T, S>, s: S) =
  p(s).also {
    assertThat(it.filterDef()).isEmpty()
  }.filterImm()

fun <T, S> getResults(p: ParserM<T, S>, s: S): List<T> = getResultsInternal(p, s).map { it.first.v }

fun <T, S> assertFail(p: ParserM<T, S>, s: S) {
  val r = getResults(p, s)
  assertThat(r).isEmpty()
}

fun <T, S> checkParser(p: ParserM<T, S>, s: S, expected: String) {
  val r = getResults(p, s)
  assertThat(r).hasSize(1)
  assertThat(r.first().toString()).isEqualTo(expected)
}

fun <T> checkParser1(p: ParserM<T, String>, inOut: String) {
  val r = getFullResults1(p, inOut)
  assertThat(r).hasSize(1)
  assertThat(r.first().toString()).isEqualTo(inOut)
}

fun <T> getFullResults1(
  p: ParserM<T, String>,
  s: String
) = getResultsInternal(p, s).filter { it.second.isEmpty() }.map { it.first.v }

fun <T> checkParser(p: ParserM<T, State<String>>, inOut: String) {
  val r = getFullResults(p, inOut)
  assertThat(r).hasSize(1)
  assertThat(r.first().toString()).isEqualTo(inOut)
}

fun <T> getFullResults(
  p: ParserM<T, State<String>>,
  s: String
) = getResultsInternal(p, State.ret(s)).filter { it.second.s.isEmpty() }.map { it.first.v }
