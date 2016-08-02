#!/bin/sh

mkdir -p ./api/src/main/scala/codegen
python project/gyb.py ./api/src/main/resources/gyb/HashEq.scala.gyb > ./api/src/main/scala/codegen/SpecializedHashEq.scala
python project/gyb.py ./api/src/main/resources/gyb/Opt.scala.gyb    > ./api/src/main/scala/codegen/SpecializedOpt.scala

mkdir -p ./specialized-core/src/main/scala/codegen
python project/gyb.py ./specialized-core/src/main/resources/gyb/BufferSeq.scala.gyb > ./specialized-core/src/main/scala/codegen/SpecializedBufferSeq.scala
python project/gyb.py ./specialized-core/src/main/resources/gyb/HashMap.scala.gyb   > ./specialized-core/src/main/scala/codegen/SpecializedHashMap.scala
python project/gyb.py ./specialized-core/src/main/resources/gyb/HashSet.scala.gyb   > ./specialized-core/src/main/scala/codegen/SpecializedHashSet.scala
