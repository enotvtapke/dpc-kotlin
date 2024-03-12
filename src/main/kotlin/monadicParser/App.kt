package monadicParser

import java.lang.StringBuilder

fun <T, S> run(parser: ParserM<T, State<S>>, initialState: State<S>): List<Result<T, State<S>>> {
  val stepResult = parser(initialState)
  val deferredResults = stepResult.filterIsInstance<Deferred<T, State<S>>>()
  val immediateResults = stepResult.filterIsInstance<Immediate<T, State<S>>>()
  return if (deferredResults.isEmpty()) immediateResults
  else if (immediateResults.isEmpty()) listOf()
  else {
    immediateResults + deferredResults.flatMap { run(it.parser, it.state) }
  }
}

private fun <T, S> fix(l: String = "self", p: (ParserM<T, S>) -> (ParserM<T, S>)) = object : ParserM<T, S> {
  override fun invoke(s: S) = p(this)(s)
  override fun toString() = l
}

abstract class BaseParser<T, S> : ParserM<T, S> {
  private var r: ParserM<T, S>? = null
  abstract val par: ParserM<T, S>

  override fun invoke(s: S): List<Result<T, S>> {
    if (r == null) r = par
    return r!!(s)
  }
}

private object C : BaseParser<CharSequence, State<CharSequence>>() {
  override val par: ParserM<CharSequence, State<CharSequence>> = def(
    C bind { cc -> term("c").map { c -> StringBuilder().append(cc as CharSequence).append(c) as CharSequence } } alt term("a")
  )
}

fun main() {
  val cc = fix { self ->
    def(
      self bind { cc -> term("c").map { c -> StringBuilder().append(cc as CharSequence).append(c) as CharSequence } } alt term("a")
    )
  }

  val c = fix { _ ->
    def(
      term("c").map { c -> StringBuilder().append(c) as CharSequence } alt term("a")
    )
  }

//  println(c(State.ret("a")))
  println(run(C, State.ret("accc")))
}