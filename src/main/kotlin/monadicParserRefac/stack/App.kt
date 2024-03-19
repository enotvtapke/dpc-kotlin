package monadicParserRefac.stack

import monadicParserRefac.stack.grammars.Lama.test
import monadicParserRefac.stack.grammars.Lama

fun main() {
  test(100, Lama.Expr)
}
