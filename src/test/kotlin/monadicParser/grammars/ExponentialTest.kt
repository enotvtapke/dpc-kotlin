package monadicParser.grammars

import monadicParser.checkParser
import monadicParser.grammars.Exponential.F
import org.junit.jupiter.api.Test

class ExponentialTest {

  @Test
  fun basic() {
    // This test has O(2^(n/2)) time complexity, when there is no memoization used. The problem is that invocation stack size is O(n).
    // As such left recursive rule E tries to compute deferred many times.
    // На каждом втором уровне стэка вызовов вызывается парсер E, вызов которого приводит к стэку вызовов высотой на 1 меньше, чем у родительского парсера.
    // Это стэк снова состоит на 50% из парсеров E, каждый из которых приводит к стэку ещё на 1 меньше.
    checkParser(F, "(((((((((((((f)))))))))))))")
  }
}