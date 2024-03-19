package parserRefac.grammars

import parserRefac.*
import parserRefac.Result

object Exponential {
  data object E : Parser {
    override fun invoke(s: String): List<Result> = (-E * !'e' + -F)(s)
  }

  data object F : Parser {
    override fun invoke(s: String): List<Result> = (!'(' * -E * !')' + !'f')(s)
  }
}