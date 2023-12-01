package org.querycrafters;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
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
import org.querycrafters.Fr94Parser;
import org.querycrafters.Utils.commonIndexer;
import org.querycrafters.Utils.CustomAnalyzer;
import org.querycrafters.parsers.TopicsParser;
import org.querycrafters.templates.Topics;

import org.apache.lucene.search.Query;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

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

            System.out.println("Indexing FR routing data");
            Fr94Parser FR94Parser = new Fr94Parser(analyzer, outputDir);
            FR94Parser.indexFr94();
        } else {
            System.out.println("Not performing indexing");
        }
        
        System.out.println("Performing search and writing results...");
        performSearchAndWriteResults2(analyzer, similarity, analyzerType, similarityType);
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

    private static void performSearchAndWriteResults2(Analyzer analyzer, Similarity similarity, String analyzerType, String similarityType) throws IOException, ParseException {
        String indexDirectoryPath = "../Assignment2/index/" + analyzerType;
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        indexSearcher.setSimilarity(similarity);

        List<String> resFileContent = new ArrayList<>();
        String topicsFilePath = "Documents/topics";
        List<Topics> topics = TopicsParser.parse(topicsFilePath);
        for (Topics topic : topics) {
            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

            // Tokenize and add terms from topic description, narrative, and title
            String[] topicFields = { "DocNo", "Title", "Date", "Author", "Content", "Section" };
            String topicQuery = topic.getTopicDesc() + " " + topic.getTopicNarrative() + " " + topic.getTopicTitle();

            // Tokenize the topic query using the analyzer
            TokenStream stream = analyzer.tokenStream(null, new StringReader(topicQuery));
            CharTermAttribute termAttribute = stream.addAttribute(CharTermAttribute.class);
            stream.reset();

            while (stream.incrementToken()) {
                // Add each tokenized term to the query
                booleanQueryBuilder.add(new TermQuery(new Term("Content", termAttribute.toString())), BooleanClause.Occur.SHOULD);
            }
            stream.end();
            stream.close();

            // Build the query
            BooleanQuery query = booleanQueryBuilder.build();

            TopDocs topDocs = indexSearcher.search(query, HITS_PER_PAGE);
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
