package monadicParser.grammars

import monadicParser.State.Companion.ret
import monadicParser.assertFail
import monadicParser.checkParser
import monadicParser.getResults
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class IndirectTest {

  @Test
  fun basic() {
    checkParser(Indirect.C, "ac")
    checkParser(Indirect.C, "acccc")
  }

  @Test
  fun veryIndirect() {
    checkParser(VeryIndirect.C, "ac")
    checkParser(VeryIndirect.C, "acccc")
  }

  @Test
  fun partial() {
    Assertions.assertThat(getResults(VeryIndirect.C, ret("acf"))).isEqualTo(listOf("a", "ac"))
    Assertions.assertThat(getResults(VeryIndirect.C, ret("acccccccfcccccccc"))).isEqualTo(listOf("a", "ac", "acc", "accc", "acccc", "accccc", "acccccc", "accccccc"))
    Assertions.assertThat(getResults(Indirect.C, ret("acccccccfcccccccc"))).isEqualTo(listOf("a", "ac", "acc", "accc", "acccc", "accccc", "acccccc", "accccccc"))
  }

  @Test
  fun fail() {
    assertFail(Indirect.C, ret(""))
    assertFail(Indirect.C, ret("d"))
    assertFail(VeryIndirect.C, ret(""))
    assertFail(VeryIndirect.C, ret("e"))
  }

  @Test
  fun complexIndirect() {
    println(ComplexIndirect.A(ret("Ededa")))
    println(ComplexIndirect.A(ret("Acbacba")))
    println(ComplexIndirect.A(ret("A")))
  }
}
