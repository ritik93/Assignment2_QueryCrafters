# Assignment2 Query-Crafters

## How to run:  

### Prerequisites
- trec_eval software must be installed under /opt to run ``run.sh``. The script assumes the software is installed in ``/opt/trec_eval-9.0.7``
- Java and Maven must be installed.

To compile and evaluate the search engine run the following from the project directory:
~~~
cd Assignment2/
./run.sh
~~~

If you want to run the scripts manually.
~~~
cd Assignment2/
mvn package
rm -r index/
rm -r results/
java -jar target/Assignment2-0.1.jar <analyzerType> <similarityType> <indexFlag>
~~~

Where:
- ``analyzerType``: have the following choices ``Standard``, ``Simple`` or ``English``, ``English-getDefaultStopSet``
- ``similarityType`` have the following choices: ``Classic``, ``BM25``, ``Boolean`` or ``LMDirichlet``
- ``indexFlag`` is an optional boolean that indexes the documents when ``yes`` is passed to it. If you don't wish to index the document again do not delete the index directory.

## Data Sets
- Financial Times Limited (1991, 1992, 1993, 1994),
- Federal Register (1994),
- Foreign Broadcast Information Service (1996)
- Los Angeles Times (1989, 1990).
