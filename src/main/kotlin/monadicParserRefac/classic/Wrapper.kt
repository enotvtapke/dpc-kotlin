package monadicParserRefac.classic

import monadicParserRefac.Def
import monadicParserRefac.Imm
import monadicParserRefac.ParserM

fun <T, S> def(p: ParserM<T, S>): ParserM<T, S> = ParserM { listOf(Pair(Def(p), it)) }

fun term(term: String): ParserM<String, String> = ParserM { s ->
  if (s.startsWith(term)) listOf(Pair(Imm(term), s.removePrefix(term))) else listOf()
}

fun term(term: Regex): ParserM<String, String> = ParserM { s ->
  val matcher = term.toPattern().matcher(s)
  if (matcher.useAnchoringBounds(false).useTransparentBounds(true).region(0, s.length).lookingAt())
    listOf(Pair(Imm(matcher.toMatchResult().group()), s.subSequence(matcher.end(), s.length).toString()))
  else listOf()
}
