package org.querycrafters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;

import org.querycrafters.FBSIParser;
import org.querycrafters.LATimesParser;
import org.querycrafters.Utils.commonIndexer;
import org.querycrafters.Utils.CustomAnalyzer;
import org.querycrafters.parsers.TopicsParser;

public class App 
{
    //Path to stopwords used in the custom analyzer
    public static String stopwords_path = "./stopwords.txt";

    public static void main(String[] args) throws IOException, ParseException {
        // example calls:
        // java -jar target/Assignment2-0.1.jar StandardAnalyzer
        // java -jar target/Assignment2-0.1.jar SimpleAnalyzer
        // java -jar target/Assignment2-0.1.jar EnglishAnalyzer
        // java -jar target/Assignment2-0.1.jar EnglishAnalyzer-getDefaultStopSet
        if (args.length < 1) {
            System.out.println("Expected arguments: <analyzerType> <similarityType>");
            System.exit(1);
        }
        String analyzerType = args[0];
        String outputDir = "../Assignment2/index/" + analyzerType;

        System.out.printf("Using Analyzer: %s\n", analyzerType);
        Analyzer analyzer = null;
        switch (analyzerType) {
            case "StandardAnalyzer":
                analyzer = new StandardAnalyzer();
                break;
            case "SimpleAnalyzer":
                analyzer = new SimpleAnalyzer();
                break;
            case "EnglishAnalyzer":
                analyzer = new EnglishAnalyzer();
                break;
            case "EnglishAnalyzer-getDefaultStopSet":
                analyzer = new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet());
                break;
            case "CustomAnalyzer":
                analyzer = new CustomAnalyzer();
                break;
            default:
                analyzer = null;
                System.out.println("Invalid analyzer type. Valid: StandardAnalyzer, SimpleAnalyzer, EnglishAnalyzer or CustomAnalyzer.");
        }

        String similarityType = args[1];
        System.out.printf("Using Similarity Score: %s\n", similarityType);
        Similarity similarity = null;
        switch (similarityType) {
            case "Classic":
                similarity = new ClassicSimilarity();
                break;
            case "BM25":
                similarity = new BM25Similarity();
            case "Boolean":
                similarity = new BooleanSimilarity();
            default:
                similarity = null;
                System.out.println("Invalid Similarity Type. Valid : Classic, BM25, or Boolean");
        }

        System.out.println("Indexing Foreign Broadcast Information Service");
        FBSIParser FBSIParser = new FBSIParser(analyzer, outputDir);
        File FBSIfolder = new File(System.getProperty("user.dir") + "/src/main/resources/Assignment Two/fbis");
        File[] FBSIfiles = FBSIfolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("fb");
            }
        });
        for (File file : FBSIfiles) {
            FBSIParser.index(file);
        }
        FBSIParser.shutdown();

        System.out.println("Indexing LATimes");
        LATimesParser LATimesParser = new LATimesParser(analyzer, outputDir);
        File LATimesfolder = new File(System.getProperty("user.dir") + "/src/main/resources/Assignment Two/latimes");
        File[] LATimesfiles = LATimesfolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("la");
            }
        });
        for (File file : LATimesfiles) {
            LATimesParser.index(file);
        }
        LATimesParser.shutdown();

        System.out.println("Indexing Financial Times");
        commonIndexer ft_indexer = new commonIndexer();
        ft_indexer.gen_ind(analyzer, similarity);

        // Todo
        System.out.println("Indexing FR routing data");

        //queries
        TopicParser tp = new TopicParser();
        String IndexDirectory = "../Assignment2/index";
        SearchClass searcher = new SearchClass(analyzer, IndexDirectory, similarity);
        List<String> queries = tp.getQueries();

        for (String query : queries) {
            System.out.println(query);
            searcher.Search("Content", query);
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
