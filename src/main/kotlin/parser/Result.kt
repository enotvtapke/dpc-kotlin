package parser

import graph.LN

sealed interface Result

data class Deferred(val data: LN, val parser: Parser, val remainder: String) : Result {
  override fun toString() = "\u27E8$parser, `$remainder`\u27E9"
}

data class Immediate(val data: LN, val remainder: String) : Result {
  override fun toString() = "\"$remainder\""
}

fun Result.fmap(f: (LN) -> LN) = when (this) {
  is Deferred -> Deferred(f(data), parser, remainder)
  is Immediate -> Immediate(f(data), remainder)
}

fun Result.data() = when (this) {
  is Deferred -> data
  is Immediate -> data
}
