package graph

import parser.Result

data class Node0<out T>(val label: T, val res: List<Result>, val children: MutableList<Node0<*>>, private val id: Long) {
  constructor(node: T) : this(node, listOf(), mutableListOf(), nextId())
  constructor(node: T, res: List<Result>) : this(node, res, mutableListOf(), nextId())

  fun <T> addChild(child: Node0<T>) {
    children.add(child)
  }

  fun leafs(): List<Node0<T>> =  (if (children.isEmpty()) listOf(this) else children.flatMap { it.leafs() }) as (List<Node0<T>>)

  fun addChild(child: Any) {
    children.add(Node0(child))
  }

  private fun toStringInternal(): String =
    buildString {
      append("$id [label=\"$label\"]")
      appendLine()
      val childrenIds = children.map(Node0<*>::id).joinToString(separator = " ")
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
    if (other !is Node0<*>) return false
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