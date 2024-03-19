package monadicParser

sealed interface Result<T, S> {
  fun mapState(f: (S) -> S): Result<T, S>
}

data class Deferred<T, S>(val parser: ParserM<T, S>, val state: S) : Result<T, S> {
  override fun mapState(f: (S) -> S): Result<T, S> = Deferred(parser, f(state))

  override fun toString() = "\u27E8$parser, \"$state\"\u27E9"
}

data class Immediate<T, S>(val res: T, val state: S) : Result<T, S> {
  override fun mapState(f: (S) -> S): Result<T, S> = Immediate(res, f(state))

  override fun toString() = "($res, \"$state\")"
}
