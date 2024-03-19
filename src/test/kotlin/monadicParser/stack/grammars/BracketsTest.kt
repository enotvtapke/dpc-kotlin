package monadicParser.stack.grammars

import monadicParser.assertFail
import monadicParser.checkParser
import monadicParser.stack.State
import org.junit.jupiter.api.Test

class BracketsTest {
  @Test
  fun basic() {
    checkParser(Brackets.F, "(feeee)")
  }

  @Test
  fun fail() {
    assertFail(Brackets.F, State("(ef)"))
  }
}