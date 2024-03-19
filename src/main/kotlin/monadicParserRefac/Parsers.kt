package monadicParserRefac

typealias Res<T, S> = Pair<Deferrable<ParserM<T, S>, T>, S>

fun interface ParserM <T, S> {
  operator fun invoke(s: S): List<Res<T, S>>

  infix fun <U> bind(f: (T) -> ParserM<U, S>): ParserM<U, S> = ParserM { s ->
    this(s).flatMap { (v, s) ->
      when(v) {
        is Imm<*, T> -> f(v.v)(s)
        is Def<ParserM<T, S>, T> -> listOf(Pair(Def(v.v bind f), s))
      }
    }
  }

  infix fun alt(p: ParserM<T, S>): ParserM<T, S> = ParserM { s -> this(s) + p(s) }

  fun <U> map(f: (T) -> U): ParserM<U, S> = this bind { ret(f(it)) }
  infix fun <U> bind(t: ParserM<U, S>): ParserM<U, S> = this bind { t }

  companion object {
    fun <S> update(f: (S) -> (S)): ParserM<S, S> = ParserM { s -> listOf(Pair(Imm(s), f(s))) }
    fun <S> fetch(): ParserM<S, S> = update { it }

    fun <T, S> ret(t: T): ParserM<T, S> = ParserM { listOf(Pair(Imm(t), it)) }
    fun <T, S> zero(): ParserM<T, S> = ParserM { listOf() }
  }
}

fun <T, S> eps(): ParserM<T?, S> = ParserM.ret(null)

fun <T, S> opt(a: ParserM<T, S>) = a as ParserM<T?, S> alt eps()

fun <S, T> List<Res<T, S>>.filterDef() =
  filter { it.first is Def<ParserM<T, S>, T> }.filterIsInstance<Pair<Def<ParserM<T, S>, T>, S>>()

fun <S, T> List<Res<T, S>>.filterImm() =
  filter { it.first is Imm<ParserM<T, S>, T> }.filterIsInstance<Pair<Imm<ParserM<T, S>, T>, S>>()

abstract class BaseParser<T, S> : ParserM<T, S> {
  private var r: ParserM<T, S>? = null
  abstract val par: ParserM<T, S>

  override fun invoke(s: S): List<Res<T, S>> {
    if (r == null) r = par
    return r!!(s)
  }
}
