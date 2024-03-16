package monadicParser.grammars

import monadicParser.State.Companion.ret
import monadicParser.assertFail
import monadicParser.checkParser
import monadicParser.getResults
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
    assertFail(C, ret(""))
    assertFail(C, ret("e"))
    assertFail(C, ret("eacccc"))
    assertFail(C, ret("fcccc"))
  }

  @Test
  fun partial() {
    assertThat(getResults(C, ret("acf"))).isEqualTo(listOf("a", "ac"))
    assertThat(getResults(C, ret("acccccccfcccccccc"))).isEqualTo(listOf("a", "ac", "acc", "accc", "acccc", "accccc", "acccccc", "accccccc"))
  }
}