package quickstart

sealed trait Color
case object Red extends Color
case object Black extends Color

case class Node(parent: Node, left: Node, right: Node, color: Color, key: Int)

class RBTree {

  def getParent(n: Node) = {
    if (n == null) null else n.parent
  }

  def getGrandParent(n: Node) = getParent(getParent(n))

  def getSibling(n: Node) = {
    val p = getParent(n)
    if (p == null) null
    else {
      if (n == p.left) p.right
      else p.left
    }
  }

  def getUncle(n: Node) = getSibling(getParent(n))



}
