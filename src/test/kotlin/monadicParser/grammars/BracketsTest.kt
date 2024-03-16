package monadicParser.grammars

import monadicParser.State.Companion.ret
import monadicParser.assertFail
import monadicParser.checkParser
import org.junit.jupiter.api.Test

class BracketsTest {
  @Test
  fun basic() {
    checkParser(Brackets.F, "(feeee)")
  }

  @Test
  fun fail() {
    assertFail(Brackets.F, ret("(ef)"))
  }
}