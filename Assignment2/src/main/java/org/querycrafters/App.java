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
    //Path to stopwords used in the custom analyzer
    public static String stopwords_path = "./stopwords.txt";
    private static int HITS_PER_PAGE = 1000; // Max number of search results per page
    private static int MAX_RESULTS = 10; // Max number of search results considered

    public static void main(String[] args) throws IOException, ParseException {
        if (args.length < 1) {
            System.out.println("Expected arguments: <analyzerType> <similarityType>");
            System.exit(1);
        }
        String analyzerType = args[0];
        String outputDir = "../Assignment2/index/" + analyzerType;

        System.out.printf("Using Analyzer: %s\n", analyzerType);
        Analyzer analyzer = null;
        switch (analyzerType) {
            case "Standard":
                analyzer = new StandardAnalyzer();
                break;
            case "Simple":
                analyzer = new SimpleAnalyzer();
                break;
            case "English":
                analyzer = new EnglishAnalyzer();
                break;
            case "English-getDefaultStopSet":
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
                break;
            case "Boolean":
                similarity = new BooleanSimilarity();
                break;
            case "LMDirichlet":
                similarity = new LMDirichletSimilarity();
                break;
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

        String indexDirectoryPath = "../Assignment2/index/" + analyzerType;
        
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        indexSearcher.setSimilarity(similarity);

        List<String> resFileContent = new ArrayList<>();
        String topicsFilePath = "Documents/topics";
        List<Topics> topicsList = TopicsParser.parse(topicsFilePath);
        // for (Topics topic : topicsList) {
        //     System.out.println(topic);
        // }
    
        parseSearch(topicsList, analyzer, indexSearcher, resFileContent, analyzerType, similarityType);

        writeResultsToFile(resFileContent, analyzerType, similarityType);
        System.out.printf("Created results file %s%s.txt\n\n", analyzerType, similarityType);

        reader.close();
        indexDirectory.close();

    }

    private static void parseSearch(List<Topics> topics, Analyzer analyzer, IndexSearcher indexSearcher, List<String> resFileContent, String analyzerType, String similarityType) throws IOException, ParseException {
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
    }

    // Write the results to the results directory
    private static void writeResultsToFile(List<String> resFileContent, String analyzerType, String similarityType) throws IOException {
        File outputDir = new File("results");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        Files.write(Paths.get("results/" + analyzerType + similarityType + ".txt"), resFileContent, Charset.forName("UTF-8"));
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
