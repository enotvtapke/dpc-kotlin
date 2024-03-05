package stackParser

sealed interface Result

data class Deferred(val parser: Parser, val state: String) : Result {
  override fun toString() = "\u27E8$parser, \"$state\"\u27E9"
}

data class Immediate(val state: String) : Result {
  override fun toString() = "\"$state\""
}
