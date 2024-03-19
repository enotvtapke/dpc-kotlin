package monadicParser.stack

import monadicParser.stack.grammars.Lama.test
import monadicParser.stack.grammars.Lama

fun main() {
  test(100, Lama.Expr)
}
