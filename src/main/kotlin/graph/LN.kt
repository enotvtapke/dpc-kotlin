package graph

data class LN(val label: String, val children: MutableList<LN>, private val id: Long) {
  constructor(label: String) : this(label, mutableListOf(), nextId())
  constructor(label: String, children: MutableList<LN>) : this(label, children, nextId())
  init {
    if (id == 103L) {
      println()
    }
  }

  fun addChild(child: LN): LN {
    children.add(child)
    return this
  }

  fun cp(): LN {
    return LN(this.label, this.children.map(LN::cp).toMutableList(), nextId())
  }

//  fun addChild(childLabel: Any): LN {
//    children.add(LN(childLabel))
//    return this
//  }

  private fun toStringInternal(): String =
    buildString {
      if (id == 103L) {
        println(111)
      }
      append("$id [label=\"${label.toString().replace("\"", "\\\"")}\"]")
      appendLine()
      val childrenIds = children.map(LN::id).joinToString(separator = " ")
      append("$id -- {$childrenIds}")
      appendLine()
      children.forEach { append(it.toStringInternal()) }
    }

  override fun toString(): String = buildString {
    append("graph ParseTree {")
    appendLine()
    append(toStringInternal().trimEnd().prependIndent("  "))
    appendLine()
    append("}")
    appendLine()
  }

  override fun equals(other: Any?): Boolean {
    if (other !is LN) return false
    return this.label == other.label && this.children == other.children
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  companion object {
    private var globalId: Long = 0
    private fun nextId() = globalId++
  }
}