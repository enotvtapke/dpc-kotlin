package monadicParser.stack

import monadicParser.*
import monadicParser.ParserM.Companion.fetch
import monadicParser.stack.InvocationStack.InvocationStackElement

fun term(term: String): ParserM<String, State<String>> = ParserM { s ->
  if (s.s.startsWith(term)) listOf(Pair(Imm(term), s.map { it.removePrefix(term) })) else listOf()
}

fun term(term: Regex): ParserM<String, State<String>> = ParserM { state ->
  val s = state.s
  val matcher = term.toPattern().matcher(s)
  if (matcher.useAnchoringBounds(false).useTransparentBounds(true).region(0, s.length).lookingAt())
    listOf(Pair(Imm(matcher.toMatchResult().group()), state.map { it.subSequence(matcher.end(), s.length).toString() }))
  else listOf()
}

data class InvocationStack<S>(val impl: List<InvocationStackElement<S>> = ArrayDeque()) : List<InvocationStackElement<S>> by impl, Cloneable {
  data class InvocationStackElement<S>(val parser: ParserM<*, State<S>>, val state: S)

  fun push(p: ParserM<*, State<S>>, s: S): InvocationStack<S> = InvocationStack(impl + InvocationStackElement(p, s))
  fun indexOf(p: ParserM<*, State<S>>, s: S): Int = impl.indexOf(InvocationStackElement(p, s))
  fun pop(): InvocationStack<S> = InvocationStack(impl.subList(0, impl.size - 1))
  fun contains(p: ParserM<*, State<S>>, s: S) = impl.contains(InvocationStackElement(p, s))

  public override fun clone() = InvocationStack(ArrayDeque(impl))
}

data class State<S>(val s: S, val invocationStack: InvocationStack<S> = InvocationStack(), val depth: Int? = null) {
  fun map(f: (S) -> S) = State(f(s), invocationStack)

  companion object {
    fun <S> ret(s: S) = State(s)
  }
}

private var memo = HashMap<Pair<ParserM<*, State<*>>, *>, List<Res<*, State<*>>>>()

fun resetTable() {
  memo = hashMapOf()
}

var counter: Int = 0

fun <T, S> run(parser: ParserM<T, State<S>>, initialState: State<S>): List<Res<T, State<S>>> {
  counter++
//  val memoized = memo[Pair(parser, initialState.s) as Pair<ParserM<*, State<*>>, *>]
//  if (memoized != null) return memoized.map { r ->
//    r.mapState { State(it.s as S, initialState.invocationStack, it.depth) }
//  } as List<Result<T, State<S>>>
  val stepResult = parser(initialState)
  val deferredResults = stepResult.filterDef()
  val immediateResults = stepResult.filterImm()
  require(deferredResults.size <= 1) { "Grammar is not LL(k)" }
  return if (deferredResults.any { it.second.depth!! < initialState.invocationStack.impl.size - 1 }) {
    immediateResults + deferredResults
  } else {
    // TODO run deferred results with states of immediate results
    val res = if (immediateResults.isEmpty()) listOf() else immediateResults + deferredResults.flatMap {
      require(it.second.invocationStack == initialState.invocationStack)
      run(
        it.first.v,
        it.second
      )
    }
//    memo[Pair(parser, initialState.s) as Pair<ParserM<*, State<*>>, *>] = res as List<Result<*, State<*>>>
    res
  }
}

fun <T, S> def(p: ParserM<T, State<S>>): ParserM<T, State<S>> = fetch<State<S>>() bind { s ->
//  println(s.invocationStack.impl.size)
  val depth = s.invocationStack.indexOf(p, s.s)
  if (depth == -1) {
    ParserM {
      run(p, State(s.s, s.invocationStack.push(p, s.s)))
        .map { result ->
          require(result.second.invocationStack.impl.last() == InvocationStackElement(p, s.s))
          Pair(result.first, result.second.copy(invocationStack = result.second.invocationStack.pop()))
        }
    }
  } else {
    ParserM {
      listOf(Pair(Def(p), s.copy(depth = depth)))
    }
  }
}
