package graph

import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz.fromGraph
import guru.nidi.graphviz.parse.Parser
import java.io.File

fun renderGraph(name: String, graph: String) {
  val file = File("./graphs/dot/$name.dot")
  file.writeText(graph)
  val g = Parser().read(file)
  fromGraph(g).render(Format.SVG).toFile(File("./graphs/svg/$name.svg"));
}