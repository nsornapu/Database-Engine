#!/bin/bash

#rm -f *.class && javac *.java && java -enableassertions Main
#rm -f *.class && javac *.java && java Main
#javac *.java && java Main
mkdir -p tedsTestClassFiles && \
  rm -f *.class && \
  rm -f tedsTestClassFiles/*.class && \
  javac *.java -d tedsTestClassFiles 2>&1 | head -10 && \
  cd tedsTestClassFiles && \
  java TedsTests 2>&1
