#!/bin/bash

#for algo in 1 2 7
for algo in 1 2 3 4 5 7
#for algo in 5
do
  #for n_user in $(seq 0 1 12)
  #do
  #  echo "Position3 4000 1 3 0 0 4 $n_user 0 0 $algo"
  #  java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 0 4 $n_user 0 0 $algo &
  #done

  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 6 0 4 0 10 0 6 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 5 0 4 1 10 0 6 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 4 0 4 2 10 0 6 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 3 0 4 3 10 0 6 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 2 0 4 4 10 0 6 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 1 0 4 5 10 0 6 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 0 0 4 6 10 0 6 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 0 0 4 7 10 0 5 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 0 0 4 8 10 0 4 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 0 0 4 9 10 0 3 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 0 0 4 10 10 0 2 0 $algo
  #java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 0 0 4 11 10 0 1 0 $algo
  java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 0 0 4 12 10 0 0 0 $algo
#  java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 0 4 13 0 0 $algo
#  java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.perfeval.Position3 4000 1 3 0 0 4 14 0 0 $algo

  wait
done
