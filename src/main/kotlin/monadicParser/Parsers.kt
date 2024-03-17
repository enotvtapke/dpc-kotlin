package monadicParser

import monadicParser.ParserM.Companion.ret

fun interface ParserM <T, S> {
  operator fun invoke(s: S): List<Result<T, S>>

  fun <U> map(f: (T) -> U): ParserM<U, S> = ParserM { s1 ->
    this(s1).map { r ->
      when (r) {
        is Immediate -> Immediate(f(r.res), r.state)
        is Deferred -> Deferred(r.parser.map(f), r.state)
      }
    }
  }

  infix fun <U> bind(t: ParserM<U, S>): ParserM<U, S> = ParserM { s ->
    this(s).flatMap { r ->
      when (r) {
        is Immediate -> t(r.state)
        is Deferred -> listOf(Deferred(r.parser bind t, r.state))
      }
    }
  }

  infix fun <U> bind(f: (T) -> ParserM<U, S>): ParserM<U, S> = ParserM { s ->
    this(s).flatMap { r ->
      when (r) {
        is Immediate -> f(r.res)(r.state)
        is Deferred -> listOf(Deferred(r.parser bind f, r.state))
      }
    }
  }

  infix fun alt(p: ParserM<T, S>): ParserM<T, S> = ParserM { s -> this(s) + p(s) }

  infix fun modify(f: (S) -> (S)): ParserM<T, S> = this.bind { t ->
    update(f).map { t }
  }

  companion object {
    fun <S> update(f: (S) -> (S)): ParserM<S, S> = ParserM { s -> listOf(Immediate(res = s, state = f(s))) }
    fun <S> fetch(): ParserM<S, S> = update { it }

    fun <T, S> ret(t: T): ParserM<T, S> = ParserM { s -> listOf(Immediate(res = t, state = s)) }
    fun <T, S> zero(): ParserM<T, S> = ParserM { listOf() }
  }
}

fun term(term: CharSequence): ParserM<CharSequence, State<CharSequence>> = ParserM { s ->
  if (s.s.startsWith(term)) listOf(Immediate(term, s.map { it.removePrefix(term) })) else listOf()
}

fun term(term: Regex): ParserM<CharSequence, State<CharSequence>> = ParserM { state ->
  val s = state.s
  val matcher = term.toPattern().matcher(s)
  if (matcher.useAnchoringBounds(false).useTransparentBounds(true).region(0, s.length).lookingAt())
    listOf(Immediate(res = matcher.toMatchResult().group(), state = state.map { it.subSequence(matcher.end(), s.length) }))
  else listOf()
}

fun <T, S> eps(): ParserM<T?, S> = ret(null)

fun <T, S> opt(a: ParserM<T, S>) = a as ParserM<T?, S> alt eps()
