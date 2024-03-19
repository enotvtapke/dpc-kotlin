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
//  fun contains(p: ParserM<*, State<S>>, s: S) = impl.lastIndexOf(InvocationStackElement(p, s)) != -1

  public override fun clone() = InvocationStack(ArrayDeque(impl))
}

data class State<S>(val s: S, val invocationStack: InvocationStack<S>, val depth: Int = -1) {
  fun map(f: (S) -> S) = State(f(s), invocationStack)

  companion object {
    fun <S> ret(s: S) = State(s, InvocationStack())
  }
}

private var memo = HashMap<Pair<ParserM<*, State<*>>, *>, List<Result<*, State<*>>>>()

fun resetTable() {
  memo = hashMapOf()
}

private fun <T, S> depth(r: Deferred<T, State<S>>): Int {
  val indexOf = r.state.invocationStack.impl.indexOf(InvocationStackElement(r.parser, r.state.s))
  require(indexOf >= 0)
  return indexOf
}

private fun <T, S> depth(invocationStack: InvocationStack<S>, p: ParserM<T, State<S>>, s: S): Int {
  val indexOf = invocationStack.impl.indexOf(InvocationStackElement(p, s))
  require(indexOf >= 0)
  return indexOf
}

fun <T, S> run(parser: ParserM<T, State<S>>, initialState: State<S>): List<Result<T, State<S>>> {
//  val memoized = memo[Pair(parser, initialState.s) as Pair<ParserM<*, State<*>>, *>]
//  if (memoized != null) return memoized.map { r ->
//    r.mapState { State(it.s as S, initialState.invocationStack, it.depth) }
//  } as List<Result<T, State<S>>>
  val stepResult = parser(initialState)
  val deferredResults = stepResult.filterIsInstance<Deferred<T, State<S>>>()
  val immediateResults = stepResult.filterIsInstance<Immediate<T, State<S>>>()
  require(deferredResults.size <= 1)
  return if (deferredResults.any { it.state.depth < initialState.invocationStack.impl.size - 1 }) {
    immediateResults + deferredResults
  } else {
    val res = if (immediateResults.isEmpty()) listOf() else immediateResults + deferredResults.flatMap {
      require(it.state.invocationStack == initialState.invocationStack)
      run(
        it.parser,
        it.state
      )
    }
//    memo[Pair(parser, initialState.s) as Pair<ParserM<*, State<*>>, *>] = res as List<Result<*, State<*>>>
    res
  }
}

fun <T, S> def(p: ParserM<T, State<S>>): ParserM<T, State<S>> = fetch<State<S>>() bind { s ->
//  println(s.invocationStack.impl.size)
  if (!s.invocationStack.contains(p, s.s)) {
    ParserM<T, State<S>> {
      val e = run(p, State(s.s, s.invocationStack.clone().push(p, s.s)))
        .map { result ->
          result.mapState {
            require(it.invocationStack.impl.last() == InvocationStackElement(p, s.s))
            it.copy(s = it.s, invocationStack = it.invocationStack.clone().pop())
          }
        }
//      println(p)
      e
    }
//    modify {
//      println(p)
//      State(it.s, it.invocationStack.clone().pop())
//    }
  } else {
    ParserM {
      val element = Deferred(p, s)
      listOf(element.copy(state = element.state.copy(depth = depth(s.invocationStack, p, s.s))))
    }
  }
}
