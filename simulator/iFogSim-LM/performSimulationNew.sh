#!/bin/bash

for algo in  5 #5 7 12
do
  for topology in A #A B C D A2 D2
  do
    for seed in 10 #10 20 30 40 50 60 70 80 90 100
    do
      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 4 0 0 0 0 0 0 0 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 6 0 4 0 0 0 6 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 5 0 4 1 10 0 6 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 4 0 4 2 10 0 6 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 3 0 4 3 10 0 6 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 2 0 4 4 10 0 6 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 1 0 4 5 10 0 6 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 6 10 0 6 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 7 10 0 5 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 8 10 0 4 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 9 10 0 3 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 10 10 0 2 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 11 10 0 1 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 12 10 0 0 0 &
#      wait
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 13 10 0 0 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 14 10 0 0 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 15 10 0 0 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 16 10 0 0 0 &
#
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 17 10 0 0 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 18 10 0 0 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 19 10 0 0 0 &
#      java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner $seed $topology $algo 3 0 0 0 4 20 10 0 0 0 &
#java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 21 10 0 0 0 &
#java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 22 10 0 0 0 &
#java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 23 10 0 0 0 &
#java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 24 10 0 0 0 &
#java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 25 10 0 0 0 &

#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 10 0 4 0 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 9 0 4 1 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 8 0 4 2 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 7 0 4 3 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 6 0 4 4 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 5 0 4 5 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 4 0 4 6 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 3 0 4 7 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 2 0 4 8 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 1 0 4 9 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 1 0 4 9 10 0 5 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 10 10 0 4 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 11 10 0 3 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 12 10 0 2 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 13 10 0 1 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 14 10 0 0 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 15 10 0 0 0 &
#    java -classpath ./fogSimulator/jars/json-simple-1.1.1.jar:./fogSimulator/jars/commons-math3-3.5/commons-math3-3.5.jar:./fogSimulator/src:./fogSimulator/output org.fog.test.my.Runner 500 $topology $algo 3 0 0 0 4 16 10 0 0 0 &
    wait
    done
  done
done
