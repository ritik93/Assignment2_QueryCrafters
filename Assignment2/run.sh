#! /usr/bin/bash

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
mvn package

# Run
java -jar target/Assignment2-0.1.jar Standard Classic
java -jar target/Assignment2-0.1.jar Standard BM25 
java -jar target/Assignment2-0.1.jar Standard Boolean
java -jar target/Assignment2-0.1.jar Standard LMDirichlet
java -jar target/Assignment2-0.1.jar Simple Classic
java -jar target/Assignment2-0.1.jar Simple BM25
java -jar target/Assignment2-0.1.jar Simple Boolean
java -jar target/Assignment2-0.1.jar Simple LMDirichlet
java -jar target/Assignment2-0.1.jar English Classic
java -jar target/Assignment2-0.1.jar English BM25
java -jar target/Assignment2-0.1.jar English Boolean
java -jar target/Assignment2-0.1.jar English LMDirichlet
java -jar target/Assignment2-0.1.jar English-getDefaultStopSet Classic
java -jar target/Assignment2-0.1.jar English-getDefaultStopSet BM25
java -jar target/Assignment2-0.1.jar English-getDefaultStopSet Boolean
java -jar target/Assignment2-0.1.jar English-getDefaultStopSet LMDirichlet

# Evaluate results
cd /opt/trec_eval-9.0.7

./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
StandardClassic.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
StandardBM25.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
StandardBoolean.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
StandardLMDirichlet.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
SimpleClassic.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
SimpleBM25.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
SimpleBoolean.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
SimpleLMDirichlet.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
EnglishClassic.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
EnglishBM25.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
EnglishBoolean.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
EnglishLMDirichlet.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
English-getDefaultStopSetClassic.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
English-getDefaultStopSetBM25.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
English-getDefaultStopSetBoolean.txt
./trec_eval -m runid -m map -m P.5 /home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/Documents/qrels.assignment2.part1 \
/home/dogriffi/infoProject/Assignment2_QueryCrafters/Assignment2/results/\
English-getDefaultStopSetLMDirichlet.txt
