package monadicParser.grammars.lama

import monadicParser.getFullResults
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.readText

class LamaTest {
  @Test
  fun basic() {
    println(getFullResults(Expr, "(1*4-32*2/12&&(2)+1)"))
  }

  @Test
  fun correctness() {
    val middle = Path("src/main/resources/lamaExpr/middle.txt").readText()
    val res = runParser(Expr, middle)
    assertThat(res).hasSize(1)
    assertThat(res.first().toString()).isEqualTo(Path("src/test/kotlin/monadicParser/grammars/lama/res.txt").readText())
  }

  @Test
  fun performance() {
    test(5)
  }
}
