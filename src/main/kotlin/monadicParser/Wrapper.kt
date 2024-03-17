package monadicParser

import monadicParser.InvocationStack.InvocationStackElement
import monadicParser.ParserM.Companion.fetch

data class InvocationStack<S>(val impl: MutableList<InvocationStackElement<S>> = ArrayDeque()) : Cloneable {
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

data class State<S>(val s: S, val invocationStack: InvocationStack<S>, val depth: Int = -1) {
  fun map(f: (S) -> S) = State(f(s), invocationStack)

  companion object {
    fun <S> ret(s: S) = State(s, InvocationStack())
  }
}

private var memo = HashMap<Pair<ParserM<*, State<*>>, State<*>>, List<Result<*, State<*>>>>()

fun resetTable() {
  memo = hashMapOf()
}

private fun <T, S> depth(r: Deferred<T, State<S>>): Int {
  val indexOf = r.state.invocationStack.impl.indexOf(InvocationStackElement(r.parser, r.state.s))
  require(indexOf >= 0)
  return indexOf
}

fun <T, S> run(parser: ParserM<T, State<S>>, initialState: State<S>): List<Result<T, State<S>>> {
  val stepResult = parser(initialState)
  val deferredResults = stepResult.filterIsInstance<Deferred<T, State<S>>>()
  val immediateResults = stepResult.filterIsInstance<Immediate<T, State<S>>>()
  require(deferredResults.size <= 1)
  return if (deferredResults.any { it.state.depth < initialState.invocationStack.impl.size - 1 }) {
    immediateResults + deferredResults
  } else {
    if (immediateResults.isEmpty()) listOf() else immediateResults + deferredResults.flatMap {
      run(
        it.parser,
        it.state.copy(invocationStack = initialState.invocationStack)
      )
    }
  }
}

fun <T, S> def1(p: ParserM<T, State<S>>): ParserM<T, State<S>> = fetch<State<S>>() bind { s ->
  if (!s.invocationStack.contains(p, s.s)) {
    ParserM {
      run(p, State(s.s, s.invocationStack.clone().push(p, s.s)))
    }
  } else {
    ParserM {
      val element = Deferred(p, s)
      listOf(element.copy(state = element.state.copy(depth = depth(element))))
    }
  }
}

// TODO memo[Pair(p, s.s) as Pair<ParserM<*, State<*>>, *>], то есть не надо учитывать invocation stack при мемоизации
fun <T, S> def(p: ParserM<T, State<S>>): ParserM<T, State<S>> = fetch<State<S>>() bind { s ->
  val memoized = memo[Pair(p, s) as Pair<ParserM<*, State<*>>, State<*>>]
  if (memoized != null) {
    ParserM { memoized as List<Result<T, State<S>>> }
  } else
    if (!s.invocationStack.contains(p, s.s)) {
      ParserM {
        run(p, State(s.s, s.invocationStack.clone().push(p, s.s))).also {
          memo[Pair(p, s) as Pair<ParserM<*, State<*>>, State<*>>] = it as List<Result<*, State<*>>>
        }
      }
    } else {
      ParserM {
        val element = Deferred(p, s)
        listOf(element.copy(state = element.state.copy(depth = depth(element))))
      }
    }
}

fun <T, S> run1(parser: ParserM<T, State<S>>, initialState: State<S>): ParserM<T, State<S>> {
  val stepResult = parser(initialState)
  val deferredResults = stepResult.filterIsInstance<Deferred<T, State<S>>>()
  val immediateResults = stepResult.filterIsInstance<Immediate<T, State<S>>>()
  require(deferredResults.size <= 1)
  return if (deferredResults.any { it.state.depth < initialState.invocationStack.impl.size - 1 }) {
    require(deferredResults.all { it.state.depth < initialState.invocationStack.impl.size - 1 })
    ParserM { immediateResults + deferredResults }
  } else {
    ParserM<T, State<S>> {
      if (immediateResults.isEmpty()) listOf() else immediateResults + deferredResults.flatMap {
        run(it.parser, it.state.copy(invocationStack = initialState.invocationStack))
      }
    } modify {
      State(it.s, it.invocationStack.clone().pop())
    }
  }
}
