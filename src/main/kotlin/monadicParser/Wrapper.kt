package monadicParser

import monadicParser.ParserM.Companion.fetch

data class InvocationStack<S>(val impl: MutableList<InvocationStackElement<S>> = ArrayDeque()): Cloneable {
  data class InvocationStackElement<S>(val parser: ParserM<*, State<S>>, val state: S)

  fun push(p: ParserM<*, State<S>>, s: S): InvocationStack<S> {
    impl.add(InvocationStackElement(p, s))
    return this
  }

  fun pop(): InvocationStack<S> {
    impl.removeLast()
    return this
  }

  fun contains(p: ParserM<*, State<S>>, s: S) = impl.contains(InvocationStackElement(p, s))

  public override fun clone() = InvocationStack(ArrayDeque(impl))
}

data class State<S>(val s: S, val invocationStack: InvocationStack<S>) {
  fun map(f: (S) -> S) = State(f(s), invocationStack)

  companion object {
    fun <S> ret(s: S) = State(s, InvocationStack())
  }
}

fun <T, S> def(p: ParserM<T, State<S>>): ParserM<T, State<S>> = fetch<State<S>>() bind { s ->
  if (!s.invocationStack.contains(p, s.s)) {
    val stepResult = (p )(State(s.s, s.invocationStack.clone().push(p, s.s)))
    val deferredResults = stepResult.filterIsInstance<Deferred<T, State<S>>>()
    val immediateResults = stepResult.filterIsInstance<Immediate<T, State<S>>>()
    (if (deferredResults.isEmpty()) ParserM<T, State<S>> { immediateResults }
    else if (immediateResults.isEmpty()) ParserM { listOf() }
    else {
      val results = immediateResults + deferredResults.flatMap { run(it.parser, it.state) }
      ParserM { results }
    }) modify { State(it.s, it.invocationStack.clone().pop()) }
  }
  else ParserM { listOf(Deferred(p, s)) }
}