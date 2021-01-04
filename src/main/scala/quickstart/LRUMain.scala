package quickstart


class LRU(val maxSize: Int) {

  var set = scala.collection.mutable.LinkedHashSet.empty[Int]

  def refer(t: Int) = {
    if (!set.contains(t)) {
      if (set.size == maxSize) {
        set = set.drop(1)
      }
    } else {
      set.remove(t)
    }
    set.add(t)
    t
  }

  override def toString() = set.mkString(", ")

}

object LRUMain extends App {
  val cache = new LRU(4)

  cache.refer(1)
  cache.refer(2)
  cache.refer(3)
  cache.refer(1)
  cache.refer(4)
  cache.refer(5)
  cache.refer(2)
  cache.refer(2)
  cache.refer(1)
  println(cache.toString())

}
