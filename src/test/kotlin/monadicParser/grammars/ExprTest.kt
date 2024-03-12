package monadicParser.grammars

import monadicParser.Deferred
import monadicParser.Immediate
import monadicParser.ParserM
import monadicParser.State
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExprTest {
  fun <T, S> check(p: ParserM<T, State<S>>, s: State<S>, expected: String) {
    val r = monadicParser.run(p, s).filterIsInstance<Immediate<Expr, State<CharSequence>>>().filter { it.state.s.isEmpty() }
    assertThat(monadicParser.run(p, s).filterIsInstance<Deferred<*, *>>()).isEmpty()
    assertThat(r).hasSize(1)
    assertThat(r.first().res.toString()).isEqualTo(expected)
  }

  fun <T, S> check(p: ParserM<T, State<S>>, inOut: S) {
    val s = State.ret(inOut)
    val r = monadicParser.run(p, s).filterIsInstance<Immediate<Expr, State<CharSequence>>>().filter { it.state.s.isEmpty() }
    assertThat(monadicParser.run(p, s).filterIsInstance<Deferred<*, *>>()).isEmpty()
    assertThat(r).hasSize(1)
    assertThat(r.first().res.toString()).isEqualTo(inOut)
  }

  @Test
  fun basic() {
    check(E, "42")
    check(E, "(42)")
    check(E, "1+1")
    check(E, "1*1")
  }

  @Test
  fun nested() {
    check(E, "(1+2)*3")
    check(E, "3*(1+2)")
    check(E, "1+(1*2)")
    check(E, "(1*2)+45")
  }

  @Test
  fun long() {
    check(E, "121+123+634+1+2+3+121+123+634+1+2+3+121+123+634+1+2+3+121+123+634+1+2+3+121+123+634+1+2+3+121+123+634+1+2+3+121+123+634+1+2+3+121+123+634+1+2+3+121+123+634+1+2+3+121+123+634+1+2+3+121+123+634+1+2+3")
    check(E, "121*123*634*1*2*3*121*123*634*1*2*3*121*123*634*1*2*3*121*123*634*1*2*3*121*123*634*1*2*3*121*123*634*1*2*3*121*123*634*1*2*3*121*123*634*1*2*3*121*123*634*1*2*3*121*123*634*1*2*3*121*123*634*1*2*3")
  }

  @Test
  fun deepNested() {
    check(E, "(((((((42)))))))")
    check(E, "((123+(3*(2+3))*2)+1+223+3423*22*(1+24+2*(23+22)))")
  }

  @Test
  fun veryDeepNested() {
    check(E, "((123+(3*(2+3))*2)+1+((123+(3*(2+3))*2)+1+223+3423*22*(1+24+2*(23+22)))+3423*22*(1+((123+(3*(2+3))*2)+1+223+3423*22*(1+24+2*(23+22)))+2*(23+22)))")
  }
}