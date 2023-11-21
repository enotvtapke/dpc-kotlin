package parserRefac

class FailedBranchesSet {
  private val failedBranches = mutableMapOf<String, MutableSet<MutableList<Token>>>()

  private fun List<Token>.hasPrefix(prefix: List<Token>): Boolean {
    if (size < prefix.size) return false
    return zip(prefix).all { (t1, t2) -> t1 == t2 }
  }

  fun add(suffix: String, branch: List<Token>) {
    val failedOnInput = failedBranches[suffix]
    if (failedOnInput == null) {
      failedBranches[suffix] = mutableSetOf(branch.toMutableList())
      return
    }
    if (failedOnInput.any { branch.hasPrefix(it) }) return
    failedOnInput.add(branch.toMutableList())
  }

  fun get() = failedBranches
  fun clear() = failedBranches.clear()

  fun size() = failedBranches.asSequence().sumOf { it.value.size }

  override fun toString(): String {
    return buildString {
      failedBranches.forEach { (k, v) ->
        append(k)
        append(":")
        append(buildString {
          v.forEach {
            appendLine()
            append(it.print)
          }
        }.prependIndent())
        appendLine()
      }
    }
  }
}