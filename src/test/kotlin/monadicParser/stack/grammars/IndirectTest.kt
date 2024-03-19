package monadicParser.stack.grammars

import monadicParser.stack.State
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
    Assertions.assertThat(getResults(VeryIndirect.C, State("acf"))).isEqualTo(listOf("a", "ac"))
    Assertions.assertThat(getResults(VeryIndirect.C, State("acccccccfcccccccc"))).isEqualTo(listOf("a", "ac", "acc", "accc", "acccc", "accccc", "acccccc", "accccccc"))
    Assertions.assertThat(getResults(Indirect.C, State("acccccccfcccccccc"))).isEqualTo(listOf("a", "ac", "acc", "accc", "acccc", "accccc", "acccccc", "accccccc"))
  }

  @Test
  fun fail() {
    assertFail(Indirect.C, State(""))
    assertFail(Indirect.C, State("d"))
    assertFail(VeryIndirect.C, State(""))
    assertFail(VeryIndirect.C, State("e"))
  }

  @Test
  fun complexIndirect() {
    println(ComplexIndirect.A(State("Ededa")))
    println(ComplexIndirect.A(State("Acbacba")))
    println(ComplexIndirect.A(State("A")))
  }
}
