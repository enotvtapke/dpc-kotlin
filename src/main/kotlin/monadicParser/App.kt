package monadicParser

import java.lang.StringBuilder

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
}
