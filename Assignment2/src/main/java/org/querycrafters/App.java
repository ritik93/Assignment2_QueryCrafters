package org.querycrafters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
// import org.jsoup.nodes.Document;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;

import org.querycrafters.FBSIParser;
import org.querycrafters.LATimesParser;
import org.querycrafters.Utils.commonIndexer;
import org.querycrafters.Utils.CustomAnalyzer;
import org.querycrafters.parsers.TopicsParser;
import org.querycrafters.templates.Topics;

import org.apache.lucene.search.Query;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import java.util.*;


public class App 
{
    
    public static String stopwords_path = "./stopwords.txt";    // Path to stopwords used in the custom analyzer
    private static int HITS_PER_PAGE = 1000;                    // Max number of search results per page
    private static int MAX_RESULTS = 10;                        // Max number of search results considered

    public static void main(String[] args) throws IOException, ParseException {
        if (args.length < 2) {
            displayUsage();;
            return;
        }
        String analyzerType = args[0];
        String similarityType = args[1];
        String performIndex = "no";
        if (args.length > 2) {
            performIndex = args[2];
        }
        
        System.out.printf("Using Analyzer: %s\n", analyzerType);
        Analyzer analyzer = createAnalyzer(analyzerType);
    
        System.out.printf("Using Similarity Score: %s\n", similarityType);
        Similarity similarity = createSimilarity(similarityType);
        
        if (performIndex.equals("yes")) {
            System.out.println("Performing indexing...");

            String outputDir = "../Assignment2/index/" + analyzerType;

            System.out.println("Indexing Foreign Broadcast Information Service...");
            FBSIParser fbsiParser = new FBSIParser(analyzer, outputDir);
            fbsiParser.indexFBSI();

            System.out.println("Indexing LATimes...");
            LATimesParser LATimesParser = new LATimesParser(analyzer, outputDir);
            LATimesParser.indexLATimes();

            System.out.println("Indexing Financial Times...");
            commonIndexer ft_indexer = new commonIndexer();
            ft_indexer.gen_ind(analyzer, similarity);

            // System.out.println("Indexing FR routing data");
            // FR94Parser FR94Parser = new FR94Parser(analyzer, outputDir);
            // FR94Parser.indexFR94Parser();
        } else {
            System.out.println("Not performing indexing");
        }
        
        System.out.println("Performing search and writing results...");
        performSearchAndWriteResults(analyzer, similarity, analyzerType, similarityType);
    }

    private static void performSearchAndWriteResults(Analyzer analyzer, Similarity similarity,  String analyzerType, String similarityType) throws IOException, ParseException {
        String indexDirectoryPath = "../Assignment2/index/" + analyzerType;
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        indexSearcher.setSimilarity(similarity);

        List<String> resFileContent = new ArrayList<>();
        String topicsFilePath = "Documents/topics";
        List<Topics> topics = TopicsParser.parse(topicsFilePath);
        for (Topics topic : topics) {
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
                    new String[]{"DocNo", "Title", "Date", "Author", "Content", "Section"},
                    analyzer);
            Query luceneQuery = queryParser.parse(topic.getTopicDesc());

            TopDocs topDocs = indexSearcher.search(luceneQuery, HITS_PER_PAGE);
            ScoreDoc[] hits = topDocs.scoreDocs;
            List<String> resultList = new ArrayList<>();
            for (int j = 0; j < hits.length && j < MAX_RESULTS; j++) {
                int docId = hits[j].doc;
                org.apache.lucene.document.Document doc = indexSearcher.doc(docId);
                resultList.add(doc.get("id"));
                resFileContent.add(topic.getTopicNum() + " Q0 " + doc.get("DocNo") + " " + (j + 1) + " " + hits[j].score + " " + analyzerType + similarityType);
            }
        }
        writeResultsToFile(resFileContent, analyzerType, similarityType);
        reader.close();
        indexDirectory.close();
    }

    // Write the results to the results directory
    private static void writeResultsToFile(List<String> resFileContent, String analyzerType, String similarityType) throws IOException {
        File resultsDir = new File("results");
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }

        Files.write(Paths.get("results/" + analyzerType + similarityType + ".txt"), resFileContent, Charset.forName("UTF-8"));
    } 

    private static void displayUsage() {
        System.out.println("Expected arguments: <analyzerType> <similarityType>");
    }

    private static Analyzer createAnalyzer(String analyzerType) {
        switch (analyzerType) {
            case "Standard":
                return new StandardAnalyzer();
            case "Simple":
                return new SimpleAnalyzer();
            case "English":
                return new EnglishAnalyzer();
            case "English-getDefaultStopSet":
                return new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet());
            case "CustomAnalyzer":
                return new CustomAnalyzer();
            default:
                System.out.println("Invalid analyzer type. Valid: StandardAnalyzer, SimpleAnalyzer, EnglishAnalyzer, or CustomAnalyzer.");
                return null;
        }
    }

    private static Similarity createSimilarity(String similarityType) {
        switch (similarityType) {
            case "Classic":
                return new ClassicSimilarity();
            case "BM25":
                return new BM25Similarity();
            case "Boolean":
                return new BooleanSimilarity();
            case "LMDirichlet":
                return new LMDirichletSimilarity();
            default:
                System.out.println("Invalid Similarity Type. Valid: Classic, BM25, Boolean, or LMDirichlet");
                return null;
        }
    }
}
// Adding modifications to App.java below
/*
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.similarities.*;

import org.querycrafters.Utils.CustomAnalyzer;

import org.apache.commons.cli.*;

public class App 
{
    private static String output_dir = "./output";
    private static String output_file = "results.txt";
    private static int max_results = 1000;
    public static String stopwords_path = "./stopwords.txt";

    public static void main( String[] args )
    {
        // Defaults for CLI args
        Analyzer analyzer = new EnglishAnalyzer();
        Similarity similarity = new BM25Similarity();

        // Setting up CLI Options
        Options options = new Options();
        options.addOption("a", "analyzer", true, "Select Analyzer (standard, whitespace, simple, english, custom)");
        options.addOption("s", "similarities", true, "Select Similarities (classic, bm25, boolean)");
        options.addOption("i", "index", false, "Generates index");
        options.addOption("h", "help", false, "Help");

        // Parse commandline arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("org.querycrafters", options);
            return;
        }

        if (cmd.hasOption("a")) {
            switch (cmd.getOptionValue("a").toLowerCase()) {
                case "standard": analyzer = new StandardAnalyzer(); break;
                case "whitespace": analyzer = new WhitespaceAnalyzer(); break;
                case "simple": analyzer = new SimpleAnalyzer(); break;
                case "english": analyzer = new EnglishAnalyzer(); break;
                case "custom": analyzer = new CustomAnalyzer(); break;
            }
        }

        if (cmd.hasOption("s")) {
            switch (cmd.getOptionValue("s").toLowerCase()) {
                case "classic": similarity = new ClassicSimilarity(); break;
                case "bm25": similarity = new BM25Similarity(); break;
                case "boolean": similarity = new BooleanSimilarity(); break;
            }
        }

    }
}
*/
