package graph

data class Node<T>(val label: T, val children: MutableList<Node<*>>, private val id: Long) {
    constructor(node: T) : this(node, mutableListOf(), nextId())

    fun <T> addChild(child: Node<T>) {
        children.add(child)
    }

    fun addChild(child: T) {
        children.add(Node(child))
    }

    private fun toStringInternal(): String =
        buildString {
            append("$id [label=\"$label\"]")
            appendLine()
            val childrenIds = children.map(Node<*>::id).joinToString(separator = " ")
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
        if (other !is Node<*>) return false
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