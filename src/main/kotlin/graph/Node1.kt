package graph

data class Node1(val label: Any, private var parent: Node1? = null) {
  private val children: MutableList<Node1> = mutableListOf()
  private val id: Long = nextId()

  fun setParent(parent: Node1) {
    this.parent = parent
    parent.children.add(this)
  }

//  fun addChild(child: Any) {
//    val element = Node1(child)
////    element.setParent(this)
//    children.add(element)
//  }

  private fun toStringInternal(): String =
    buildString {
      append("$id [label=\"$label\"]")
      appendLine()
      val childrenIds = children.map(Node1::id).joinToString(separator = " ")
      append("$id -- {$childrenIds}")
      appendLine()
      children.forEach { append(it.toStringInternal()) }
    }

//  override fun toString(): String = buildString {
//    append("graph ParseTree {")
//    appendLine()
//    append(toStringInternal().trimEnd().prependIndent("  "))
//    appendLine()
//    append("}")
//    appendLine()
//  }

  override fun equals(other: Any?): Boolean {
    if (other !is Node1) return false
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