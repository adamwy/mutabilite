package offheap.collection.macros

import scala.reflect.macros.whitebox

class SeqOpsMacros(val c: whitebox.Context) extends Common {

  import c.universe._
  import c.universe.definitions._

  def stabilizedSeq[A: WeakTypeTag](f: Tree => Tree): Tree =
    stabilized(c.prefix.tree) { pre =>
      val seqTpe = seqType[A]
      val seq = freshVal("seq", seqTpe, q"$pre.seq")
      q"""
        $seq
        ..${f(q"${seq.symbol}")}
      """
    }

  def map[A: WeakTypeTag, B: WeakTypeTag](f: Tree) =
    stabilizedSeq[A] { seq =>
      val builderTpe = bufferType[B]
      val builder = freshVal("builder",
                             builderTpe,
                             q"new $builderTpe(initialSize = $seq.capacity)")
      val body = iterateSeq(
          seq,
          idx => q"${builder.symbol}.append(${app(f, q"$seq($idx)")})"
      )
      q"""
        $builder
        ..$body
        ${builder.symbol}
      """
    }

  def flatMap[A: WeakTypeTag, B: WeakTypeTag](f: Tree) =
    stabilizedSeq[A] { seq =>
      val builderTpe = bufferType[B]
      val builder = freshVal("builder",
                             builderTpe,
                             q"new $builderTpe(initialSize = $seq.capacity)")
      val body = iterateSeq(seq, idx => {
        val result =
          freshVal("result", builderTpe, q"${app(f, q"$seq($idx)")}")
        val inner = iterateSeq(
            q"${result.symbol}",
            idx2 => q"${builder.symbol}.append(${result.symbol}($idx2))"
        )
        q"""
          $result
          ..$inner
        """
      })
      q"""
        $builder
        ..$body
        ${builder.symbol}
      """
    }

  def filter[A: WeakTypeTag](f: Tree) =
    stabilizedSeq[A] { seq =>
      val A = weakTypeOf[A]
      val builderTpe = bufferType[A]
      val builder = freshVal("builder", builderTpe, q"new $builderTpe")
      val body = iterateSeq(seq, idx => {
        val el = freshVal("el", A, q"$seq($idx)")
        q"""
          $el
          if (${app(f, q"${el.symbol}")}) ${builder.symbol}.append(${el.symbol})
        """
      })
      q"""
        $builder
        ..$body
        ${builder.symbol}
      """
    }

  def foreach[A: WeakTypeTag](f: Tree) =
    stabilizedSeq[A] { seq =>
      iterateSeq(seq, idx => app(f, q"$seq($idx)"))
    }

  def foldLeft[A: WeakTypeTag, B: WeakTypeTag](z: Tree)(op: Tree) =
    stabilizedSeq[A] { seq =>
      stabilized(z) { z =>
        val accTpe = weakTypeOf[B]
        val acc = freshVar("acc", accTpe, q"$z")
        val body = iterateSeq(
            seq,
            idx =>
              q"${acc.symbol} = ${app(op, q"${acc.symbol}", q"$seq($idx)")}")
        q"""
          $acc
          ..$body
          ${acc.symbol}
        """
      }
    }

  def foldRight[A: WeakTypeTag, B: WeakTypeTag](z: Tree)(op: Tree) =
    stabilizedSeq[A] { seq =>
      stabilized(z) { z =>
        val accTpe = weakTypeOf[B]
        val acc = freshVar("acc", accTpe, q"$z")
        val body = iterateSeqReverse(
            seq,
            idx =>
              q"${acc.symbol} = ${app(op, q"$seq($idx)", q"${acc.symbol}")}")
        q"""
          $acc
          ..$body
          ${acc.symbol}
        """
      }
    }

  def reduceLeft[A: WeakTypeTag](op: Tree) =
    stabilizedSeq[A] { seq =>
      val accTpe = weakTypeOf[A]
      val acc = freshVar("acc", accTpe, q"$seq(0)")
      val idx = freshVar("i", IntTpe, q"1")
      val size = freshVal("size", IntTpe, q"$seq.size")
      q"""
        $acc
        $idx
        $size
        if (${size.symbol} == 0) ${throwUnsupportedOperation("empty.reduce")}
        while (${idx.symbol} < ${size.symbol}) {
          ${acc.symbol} = ${app(op,
                                q"${acc.symbol}",
                                q"${seq.symbol}(${idx.symbol})")}
          ${idx.symbol} += 1
        }
        ${acc.symbol}
      """
    }

  def reduceRight[A: WeakTypeTag](op: Tree) =
    stabilizedSeq[A] { seq =>
      val accTpe = weakTypeOf[A]
      val acc = freshVar("acc", accTpe, q"$seq($seq.size - 1)")
      val idx = freshVar("i", IntTpe, q"$seq.size - 2")
      q"""
        $acc
        $idx
        if ($seq.size == 0) ${throwUnsupportedOperation("empty.reduce")}
        while (${idx.symbol} >= 0) {
          ${acc.symbol} = ${app(op,
                                q"${seq.symbol}(${idx.symbol})",
                                q"${acc.symbol}")}
          ${idx.symbol} -= 1
        }
        ${acc.symbol}
      """
    }
}
