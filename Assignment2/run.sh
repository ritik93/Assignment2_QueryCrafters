#! /usr/bin/bash

export INFO_HOME=$(pwd)

if [ -d "index" ]; then
    rm -r "index"
    echo "Index deleted"
else
    echo "Index does not exist"
fi

if [ -d "results" ]; then
    rm -r "results"
    echo "Results folder deleted"
else
    echo "Results folder does not exist"
fi

# Build
# mvn package

# Run
java -jar target/Assignment2-0.1.jar Standard Classic yes
java -jar target/Assignment2-0.1.jar Standard BM25
java -jar target/Assignment2-0.1.jar Standard Boolean
java -jar target/Assignment2-0.1.jar Standard LMDirichlet
java -jar target/Assignment2-0.1.jar Simple Classic yes
java -jar target/Assignment2-0.1.jar Simple BM25
java -jar target/Assignment2-0.1.jar Simple Boolean
java -jar target/Assignment2-0.1.jar Simple LMDirichlet
java -jar target/Assignment2-0.1.jar English Classic yes
java -jar target/Assignment2-0.1.jar English BM25
java -jar target/Assignment2-0.1.jar English Boolean
java -jar target/Assignment2-0.1.jar English LMDirichlet
java -jar target/Assignment2-0.1.jar English-getDefaultStopSet Classic yes
java -jar target/Assignment2-0.1.jar English-getDefaultStopSet BM25
java -jar target/Assignment2-0.1.jar English-getDefaultStopSet Boolean
java -jar target/Assignment2-0.1.jar English-getDefaultStopSet LMDirichlet

# Evaluate results
cd /opt/trec_eval-9.0.7

./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
StandardClassic.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
StandardBM25.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
StandardBoolean.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
StandardLMDirichlet.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
SimpleClassic.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
SimpleBM25.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
SimpleBoolean.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
SimpleLMDirichlet.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
EnglishClassic.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
EnglishBM25.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
EnglishBoolean.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
EnglishLMDirichlet.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
English-getDefaultStopSetClassic.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
English-getDefaultStopSetBM25.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
English-getDefaultStopSetBoolean.txt
./trec_eval -m runid -m map -m P.5 $INFO_HOME/Documents/qrels.assignment2.part1 \
$INFO_HOME/results/\
English-getDefaultStopSetLMDirichlet.txt
