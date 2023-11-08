package parser

import graph.Node

sealed interface Result

data class Deferred(val data: Node, val parser: Parser, val remainder: String) : Result {
  override fun toString() = "\u27E8$parser, \"$remainder\"\u27E9"
}

data class Immediate(val data: Node, val remainder: String) : Result {
  override fun toString() = "\"$remainder\""
}

fun Result.fmap(f: (Node) -> Node) = when (this) {
  is Deferred -> Deferred(f(data), parser, remainder)
  is Immediate -> Immediate(f(data), remainder)
}

fun Result.data() = when (this) {
  is Deferred -> data
  is Immediate -> data
}
