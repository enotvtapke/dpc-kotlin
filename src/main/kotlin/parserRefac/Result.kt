package parserRefac

data class Token(val c: Char) {
  override fun toString() = "$c"
}

sealed class Result(open val consumed: List<Token>, open val remainder: String)

data class Deferred(
  override val consumed: List<Token>,
  val parser: Parser,
  override val remainder: String
) : Result(consumed, remainder) {
  override fun toString() = "\u27E8${consumed.print}, $parser, `$remainder`\u27E9"
}

data class Immediate(override val consumed: List<Token>, override val remainder: String) : Result(consumed, remainder) {
  override fun toString() = "+${consumed.print} `$remainder`+"
}

data class Failed(override val consumed: List<Token>, override val remainder: String) : Result(consumed, remainder) {
  override fun toString() = "-${consumed.print} `$remainder`-"
}

private val List<Token>.print get() = "\"" + joinToString("") + "\""