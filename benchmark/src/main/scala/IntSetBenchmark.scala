package benchmark

import org.openjdk.jmh.annotations._
import offheap.collection._
import HashEq.Implicits._
import org.openjdk.jmh.infra.Blackhole

import scala.collection.mutable.{HashSet => StdlibSet}

@State(Scope.Thread)
class IntSetBenchmark {

  import Benchmark._

  implicit val allocator = scala.offheap.malloc

  val offheapSet: OffheapHashSet_Int = {
    val set = OffheapSet_Int.create(initialSize)
    var i = 0
    while (i < size) {
      set.add(i)
      i += 1
    }
    set
  }

  val specSet: HashSet_Int = {
    val set = new HashSet_Int(initialSize)
    var i = 0
    while (i < size) {
      set.add(i)
      i += 1
    }
    set
  }

  val genericSet: HashSet[Int] = {
    val set = new HashSet[Int](initialSize)
    var i = 0
    while (i < size) {
      set.add(i)
      i += 1
    }
    set
  }

  val stdSet: StdlibSet[Int] = {
    val set = StdlibSet[Int]()
    var i = 0
    while (i < size) {
      set.add(i)
      i += 1
    }
    set
  }

  var freedSet: OffheapHashSet_Int = _

  var randKey: Int = _
  var nonExistingKey: Int = _

  @Setup(Level.Invocation)
  def setup = {
    randKey = random.nextInt(size)
    nonExistingKey = randKey + size
    freedSet = OffheapHashSet_Int.empty
  }

  @TearDown(Level.Invocation)
  def tearDown = if (freedSet.nonEmpty) freedSet.free

  @Benchmark
  def containsExistingOffheap = offheapSet(randKey)

  @Benchmark
  def containsExistingSpecialized = specSet(randKey)

  @Benchmark
  def containsExistingGeneric = genericSet(randKey)

  @Benchmark
  def containsExistingStdlib = stdSet(randKey)

  @Benchmark
  def containsNonExistingOffheap = offheapSet(nonExistingKey)

  @Benchmark
  def containsNonExistingSpecialized = specSet(nonExistingKey)

  @Benchmark
  def containsNonExistingGeneric = genericSet(nonExistingKey)

  @Benchmark
  def containsNonExistingStdlib = stdSet(nonExistingKey)

  @Benchmark
  def addOffheap = {
    val freedSet = OffheapSet_Int.create(initialSize)
    var i = 0
    while (i < size) {
      freedSet.add(i)
      i += 1
    }
  }

  @Benchmark
  def addSpecialized = {
    val s = new HashSet_Int(initialSize)
    var i = 0
    while (i < size) {
      s.add(i)
      i += 1
    }
  }

  @Benchmark
  def addGeneric = {
    val s = new HashSet[Int](initialSize)
    var i = 0
    while (i < size) {
      s.add(i)
      i += 1
    }
  }

  @Benchmark
  def addStdlib = {
    val s = StdlibSet[Int]()
    var i = 0
    while (i < size) {
      s.add(i)
      i += 1
    }
  }

  @Benchmark
  def foreachOffheap(blackhole: Blackhole) =
    offheapSet foreach (blackhole.consume(_))

  @Benchmark
  def foreachSpecialized(blackhole: Blackhole) =
    specSet foreach (blackhole.consume(_))

  @Benchmark
  def foreachGeneric(blackhole: Blackhole) =
    genericSet foreachGeneric (blackhole.consume(_))

  @Benchmark
  def foreachStdlib(blackhole: Blackhole) =
    stdSet foreach (blackhole.consume(_))
}

@State(Scope.Thread)
class IntSetRemoveOffheapBenchmark {

  import Benchmark._

  implicit val allocator = scala.offheap.malloc

  var set: OffheapHashSet_Int = _

  @Setup(Level.Invocation)
  def setup = {
    set = OffheapSet_Int.create(initialSize)
    var i = 0
    while (i < size) {
      set.add(i)
      i += 1
    }
  }

  @TearDown(Level.Invocation)
  def tearDown = set.free

  @Benchmark
  def benchmark = {
    var i = 0
    while (i < size / 10) { set.remove(i * 10); i += 1 }
  }
}

@State(Scope.Thread)
class IntSetRemoveSpecializedBenchmark {

  import Benchmark._

  var set: HashSet_Int = _

  @Setup(Level.Invocation)
  def setup = {
    set = new HashSet_Int(initialSize)
    var i = 0
    while (i < size) {
      set.add(i)
      i += 1
    }
  }

  @Benchmark
  def benchmark = {
    var i = 0
    while (i < size / 10) { set.remove(i * 10); i += 1 }
  }
}

@State(Scope.Thread)
class IntSetRemoveGenericBenchmark {

  import Benchmark._

  var set: HashSet[Int] = _

  @Setup(Level.Invocation)
  def setup = {
    set = new HashSet[Int](initialSize)
    var i = 0
    while (i < size) {
      set.add(i)
      i += 1
    }
  }

  @Benchmark
  def benchmark = {
    var i = 0
    while (i < size / 10) { set.remove(i * 10); i += 1 }
  }
}

@State(Scope.Thread)
class IntSetRemoveStdlibBenchmark {

  import Benchmark._

  var set: StdlibSet[Int] = _

  @Setup(Level.Invocation)
  def setup = {
    set = StdlibSet[Int]()
    var i = 0
    while (i < size) {
      set.add(i)
      i += 1
    }
  }

  @Benchmark
  def benchmark = {
    var i = 0
    while (i < size / 10) { set.remove(i * 10); i += 1 }
  }
}
