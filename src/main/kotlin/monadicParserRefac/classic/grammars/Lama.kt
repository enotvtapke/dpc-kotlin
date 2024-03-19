package monadicParserRefac.classic.grammars

import monadicParserRefac.*
import monadicParserRefac.ParserM.Companion.ret
import monadicParserRefac.BaseParser
import monadicParserRefac.classic.def
import monadicParserRefac.classic.term
import kotlin.io.path.Path
import kotlin.io.path.appendText
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.measureTime

typealias Parser <T> = ParserM<T, String>
typealias Par <T> = BaseParser<T, String>

object Lama {
  fun token(term: String): Parser<String> = term(term) bind { whitespace(); ret(it) }
  fun token(term: Regex): Parser<String> = term(term) bind { whitespace(); ret(it) }

  fun whitespace(): Parser<Unit> = term("\\s*".toRegex()).map { }

  sealed interface Primary
  data class PrimaryConst(val v: Double) : Primary {
    override fun toString() = "Const (${v.toLong()})"
  }

  data class PrimaryVar(val v: String) : Primary {
    override fun toString() = "Var (\"$v\")"
  }

  data class Binop(val op: String, val l: Primary, val r: Primary) : Primary {
    override fun toString() = "Binop (\"$op\", $l, $r)"
  }

  private fun <X, Y, S> left(s: (X, Y) -> S, c: (X) -> X, x: X, y: Y) = s(c(x), y)

  fun expr(operations: List<Parser<(Primary, Primary) -> Binop>>, operand: Parser<Primary>): Parser<Primary> {
    val n = operations.size
    val operationsAssoc = operations.map {
      it.map<((Primary) -> Primary, Primary, Primary) -> Primary> { o -> { c, x, y -> left(o, c, x, y) } }
    }

    fun inner(l: Int, c: (Primary) -> Primary): Parser<Primary> {
      return when (l) {
        n -> operand.map { c(it) }
        else -> inner(l + 1) { it } bind { x ->
          opt(operationsAssoc[l].bind { o -> inner(l) { o(c, x, it) } }).map { b -> b ?: c(x) }
        }
      }
    }

    return inner(0) { it }
  }

  fun binop(op: String): Parser<(Primary, Primary) -> Binop> = token(op).map { { l, r -> Binop(op, l, r) } }

  object Expr : Par<Primary>() {
    override val par: Parser<Primary>
      get() = def(Basic)
  }

  object Prim : Par<Primary>() {
    override val par: Parser<Primary>
      get() = def(
        token("\\d+(\\.\\d+)?".toRegex()).map { PrimaryConst(it.toString().toDouble()) as Primary } alt
                token("[a-z][a-z_A-Z0-9']*".toRegex()).map { v -> PrimaryVar(v.toString()) } alt
                (token("(") bind Expr bind { e -> token(")").map { e } })
//              alt (Expr.map { PrimaryExpr(it) })
      )
  }

  object Basic : Par<Primary>() {
    override val par: Parser<Primary>
      get() = def(
        expr(
          listOf(
            binop("!!"),
            binop("&&"),
            binop("=="),
            binop("!="),
            binop("<"),
            binop(">"),
            binop("<="),
            binop(">="),
            binop("+"),
            binop("-"),
            binop("*"),
            binop("/"),
            binop("%"),
          ), Prim
        )
      )
  }

  private fun <T> runParser(parser: ParserM<T, String>, input: String): List<T> {
    val results: MutableList<T> = mutableListOf()
    val out = Path("./out.txt")
    out.writeText("")

    parser(input).forEach { result ->
      require(result.first is Imm<*, T>)
      if (result.second.isEmpty()) {
        results += (result.first as Imm<*, T>).v
        out.appendText((result.first as Imm<*, T>).v.toString() + "\n")
      }
    }
    return results
  }

  fun <T> test(iter: Int, parser: ParserM<T, String>) {
    val left = Path("src/main/resources/lamaExpr/left.txt").readText()
    val right = Path("src/main/resources/lamaExpr/right.txt").readText()
    val middle = Path("src/main/resources/lamaExpr/middle.txt").readText()

    val lastInputFile = Path("src/main/resources/lamaExpr/lastInput.txt")

    val logFile = Path("src/main/resources/lamaExpr/log.txt")
    logFile.writeText("input_size time\n")

    for (i in iter - 1..<iter) {
      val full = left.repeat(i) + middle + right.repeat(i)
      lastInputFile.writeText(full)
      val numOfIter = 5
      val meanTime = generateSequence {
        measureTime {
          check(runParser(parser, full).size == 1)
        }
      }.take(numOfIter).sumOf { it.inWholeMilliseconds }.toDouble() / numOfIter
      logFile.appendText("${full.length} $meanTime\n")
    }
  }
}