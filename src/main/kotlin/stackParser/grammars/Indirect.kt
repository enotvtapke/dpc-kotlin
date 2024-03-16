package stackParser.grammars

import stackParser.*


object Indirect {
  data object C: BaseParser() {
    override val par: Parser
      get() = seq(-B, !"с") + !"a"
  }

  data object B : BaseParser() {
    override val par: Parser = -C
  }

}

fun main() {
}