package monadicParserRefac.stack.grammars

import monadicParserRefac.stack.assertFail
import monadicParserRefac.stack.checkParser
import monadicParserRefac.stack.State
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