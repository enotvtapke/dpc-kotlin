package monadicParserRefac

sealed interface Deferrable<D, I>
data class Def<D, I>(val v: D): Deferrable<D, I>
data class Imm<D, I>(val v: I): Deferrable<D, I>
