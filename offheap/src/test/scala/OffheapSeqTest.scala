package test

import offheap.collection._
import org.scalatest.{BeforeAndAfter, FunSuite}

import HashEq.Implicits._

class OffheapSeqTest extends FunSuite with BeforeAndAfter {
  implicit val alloc = scala.offheap.malloc

  var seq: OffheapBufferSeq_Int = _

  before {
    seq = OffheapSeq_Int.create()
    1 to 10 foreach (seq.append(_))
  }

  after {
    seq.free
  }

  test("isEmpty") {
    assert(seq.notEmpty)
    assert(OffheapSeq_Int.create().empty)
  }

  test("size") {
    assert(seq.size == 10)
  }

  test("foreach") {
    var sum = 0

    seq.foreach(sum += _)

    assert(sum == 10 * 11 / 2)
  }

  test("apply") {
    for (i <- 0 until 10) {
      assert(seq(i) == i + 1)
    }
  }

  test("append") {
    val seq = OffheapSeq_Int.create()

    assert(seq.empty)
    1 to 3 foreach (seq.append(_))
    assert(seq.notEmpty)
    assert(seq.size == 3)
  }

  test("prepend") {
    val seq = OffheapSeq_Int.create()

    1 to 10 foreach (seq.insert(0, _))
    assert(seq.size == 10)
  }

  test("update") {
    seq(0) = 100
    assert(seq(0) == 100)
  }

  test("remove") {
    seq.remove(1)
    assert(seq.size == 9)

    assert(seq(0) == 1)
    for (i <- 1 until 9) {
      assert(seq(i) == i + 2)
    }

    seq.remove(seq.size - 1)
    assert(seq.size == 8)
  }

  test("index") {
    for (i <- 1 to 10) {
      assert(seq.index(i) == i - 1)
    }
    assert(seq.index(0) == -1)
  }

  test("insert") {
    seq.insert(1, 50)
    assert(seq.size == 11)

    assert(seq(0) == 1)
    assert(seq(1) == 50)
    for (i <- 2 until 10) {
      assert(seq(i) == i)
    }
  }

  test("map_Int") {
    val seq: OffheapBufferSeq_Int = OffheapSeq_Int.create()
    1 to 3 foreach (seq.append(_))

    val mapped: OffheapBufferSeq_Int = seq map_Int (_ * 2)
    assert(mapped.size == 3)
    1 to 3 foreach (i => assert(mapped(i - 1) == i * 2))
  }

  test("flatMap_Int") {
    val seq: OffheapBufferSeq_Int = OffheapSeq_Int.create()
    1 to 5 by 2 foreach (seq.append(_))

    val mapped: OffheapBufferSeq_Int = seq flatMap_Int { i =>
      val r = OffheapSeq_Int.create()
      r.append(i)
      r.append(i + 1)
      r
    }
    assert(mapped.size == 6)
    1 to 6 foreach (i => assert(mapped(i - 1) == i))
  }

  test("filter") {
    val seq: OffheapBufferSeq_Int = OffheapSeq_Int.create()
    1 to 10 foreach (seq.append(_))

    val filtered = seq filter (i => i % 2 == 0)
    val test: OffheapBufferSeq_Int = filtered

    assert(filtered.size == 5)
    2 to 10 by 2 foreach (i => assert(filtered((i - 1) / 2) == i))
  }
}