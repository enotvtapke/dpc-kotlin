package parser1

sealed interface Result

data class Deferred(val parser: Parser, val remainder: String) : Result {
  override fun toString() = "\u27E8$parser, `$remainder`\u27E9"
}

data class Immediate(val remainder: String) : Result {
  override fun toString() = "\"$remainder\""
}
