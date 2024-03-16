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
  fun first(p: ParserM<*, State<S>>, s: S) = impl.size != 0 && impl.indexOf(InvocationStackElement(p, s)) == impl.size - 1

  public override fun clone() = InvocationStack(ArrayDeque(impl))
}

data class State<S>(val s: S, val invocationStack: InvocationStack<S>, val depth: Int = -1) {
  fun map(f: (S) -> S) = State(f(s), invocationStack)

  companion object {
    fun <S> ret(s: S) = State(s, InvocationStack())
  }
}

private val memo = HashMap<Pair<ParserM<*, State<*>>, State<*>>, List<Result<*, State<*>>>>()

private object Counter {
  var i: Int = 0
}

fun <T, S> depth(r: Deferred<T, State<S>>): Int {
  val indexOf = r.state.invocationStack.impl.indexOf(InvocationStackElement(r.parser, r.state.s))
  require(indexOf >= 0) {
    "a"
  }
  return indexOf
}

fun <T, S> run(parser: ParserM<T, State<S>>, initialState: State<S>): List<Result<T, State<S>>> {
  val stepResult = parser(initialState)
  val deferredResults = stepResult.filterIsInstance<Deferred<T, State<S>>>()
  val immediateResults = stepResult.filterIsInstance<Immediate<T, State<S>>>()
  return if (deferredResults.isEmpty()) immediateResults
  else if (immediateResults.isEmpty()) {
    if (deferredResults.any { it.state.depth < initialState.invocationStack.impl.size - 1 }) {
      deferredResults
    } else {
      listOf()
    }
  }
//  else immediateResults + deferredResults.flatMap { run(it.parser, it.state.copy(invocationStack = InvocationStack())) } // clear stack
  else immediateResults + open(deferredResults)
//    else immediateResults + deferredResults.flatMap { run(it.parser, it.state) }
}

fun <T, S> open(r: List<Deferred<T, State<S>>>): List<Immediate<T, State<S>>> {
  val res = mutableListOf<Immediate<T, State<S>>>()
  var deferredResults = r
//  var tmpRes = r.flatMap { run(it.parser, it.state) }
//  var deferredResults = tmpRes.filterIsInstance<Deferred<T, State<S>>>()
//  var immediateResults = tmpRes.filterIsInstance<Immediate<T, State<S>>>()
//  res += immediateResults
  while (deferredResults.isNotEmpty()) {
    val tmpRes = deferredResults.flatMap { run(it.parser, it.state) } // TODO clear stack?
    deferredResults = tmpRes.filterIsInstance<Deferred<T, State<S>>>()
    val immediateResults = tmpRes.filterIsInstance<Immediate<T, State<S>>>()
    res += immediateResults
  }
  return res
}

fun <T, S> def(p: ParserM<T, State<S>>): ParserM<T, State<S>> = fetch<State<S>>() bind { s ->
  if (!s.invocationStack.contains(p, s.s)) {
    println()
    ParserM<T, State<S>> {
//      val stepResult = p(State(s.s, s.invocationStack.clone().push(p, s.s)))
//      val deferredResults = stepResult.filterIsInstance<Deferred<T, State<S>>>()
//      val immediateResults = stepResult.filterIsInstance<Immediate<T, State<S>>>()
//      if (deferredResults.isEmpty()) immediateResults
//      else if (immediateResults.isEmpty()) {
//        listOf()
////        if (s.invocationStack.first(p, s.s)) listOf() else deferredResults
//      } else immediateResults + deferredResults.flatMap { run(it.parser, it.state) }
//      p(State(s.s, s.invocationStack.clone().push(p, s.s)))
      run(p, State(s.s, s.invocationStack.clone().push(p, s.s)))
    } modify {
      State(it.s, it.invocationStack.clone().pop())
    }
  } else {
    ParserM {
      val element = Deferred(p, s)
      listOf(element.copy(state = element.state.copy(depth = depth(element))))
    }
  }
}

// TODO memo[Pair(p, s.s) as Pair<ParserM<*, State<*>>, *>], то есть не надо учитывать invocation stack при мемоизации
fun <T, S> def1(p: ParserM<T, State<S>>): ParserM<T, State<S>> = fetch<State<S>>() bind { s ->
  val memoized = memo[Pair(p, s) as Pair<ParserM<*, State<*>>, State<*>>]
  if (memoized != null) {
    ParserM { memoized as List<Result<T, State<S>>> }
  }
  else
    if (!s.invocationStack.contains(p, s.s)) {
      println()
      ParserM<T, State<S>> {
        run(p, State(s.s, s.invocationStack.clone().push(p, s.s))).also {
          memo[Pair(p, s) as Pair<ParserM<*, State<*>>, State<*>>] = it as List<Result<*, State<*>>>
        }
      } modify {
        State(it.s, it.invocationStack.clone().pop())
      }
    } else {
      ParserM {
        listOf(Deferred(p, s))
      }
    }
}
