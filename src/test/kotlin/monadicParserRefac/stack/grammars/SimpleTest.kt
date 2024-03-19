package monadicParserRefac.stack.grammars

import monadicParserRefac.stack.State
import monadicParserRefac.stack.assertFail
import monadicParserRefac.stack.checkParser
import monadicParserRefac.stack.getResults
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SimpleTest {
  @Test
  fun basic() {
    checkParser(C, "accc")
    checkParser(C, "a")
    checkParser(C, "accccccccccccccccccc")
  }

  @Test
  fun fail() {
    assertFail(C, State(""))
    assertFail(C, State("e"))
    assertFail(C, State("eacccc"))
    assertFail(C, State("fcccc"))
  }

  @Test
  fun partial() {
    assertThat(getResults(C, State("acf"))).isEqualTo(listOf("a", "ac"))
    assertThat(getResults(C, State("acccccccfcccccccc"))).isEqualTo(listOf("a", "ac", "acc", "accc", "acccc", "accccc", "acccccc", "accccccc"))
  }
}
