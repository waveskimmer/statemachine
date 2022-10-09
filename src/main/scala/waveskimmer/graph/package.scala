package waveskimmer

package object graph {

  case class Node(name: String) {
    def >>(to: Node): Edge = new Edge(this, to)
  }

  class Edge(from: Node, to: Node, bidirectional: Boolean = true)

  class Graph(edges: List[Edge])

}
