package monadicParser.stack.grammars

import monadicParser.stack.State
import monadicParser.stack.grammars.Lama.runParser
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.readText

class LamaTest {
  @Test
  fun correctness() {
    val middle = Path("src/main/resources/lamaExpr/middle.txt").readText()
    val res = runParser(Lama.Expr, State(middle))
    Assertions.assertThat(res).hasSize(1)
    Assertions.assertThat(res.first().toString()).isEqualTo(Path("/home/enotvtapke/thesis/dpc-kotlin/src/test/resources/res.txt").readText())
  }

  @Test
  fun performance() {
    Lama.test(20, Lama.Expr)
  }
}