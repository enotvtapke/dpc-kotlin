package parserRefac.grammars

import parserRefac.*
import parserRefac.Result

data object E : Parser {
  override fun invoke(s: String): List<Result> = (-E * !'+' * T + T)(s)
}

data object T : Parser {
  override fun invoke(s: String): List<Result> = (-T * !'*' * F + F)(s)
}

data object F : Parser {
  override fun invoke(s: String): List<Result> = (!'(' * E * !')'  + !'1')(s)
}


