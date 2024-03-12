package monadicParser

sealed interface Result<T, S>

data class Deferred<T, S>(val parser: ParserM<T, S>, val state: S) : Result<T, S> {
  override fun toString() = "\u27E8$parser, \"$state\"\u27E9"
}

data class Immediate<T, S>(val res: T, val state: S) : Result<T, S> {
  override fun toString() = "($res, \"$state\")"
}
