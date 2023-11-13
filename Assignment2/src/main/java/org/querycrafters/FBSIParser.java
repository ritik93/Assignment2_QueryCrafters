package org.querycrafters;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.nio.charset.StandardCharsets;


public class FBSIParser {
    public static void main(String[] args) throws IOException {

        // Usage
        if (args.length <= 2) {
            System.out.println("Expected arguments: <analyzerType> <indexDirectory> <documentFilePath>");
            System.exit(1);
        }
        
        String analyzerType = args[0];        
        String indexDirectory = args[1] + "/" + analyzerType;
        String filePath = args[2];

        System.out.printf("Using Analyzer: %s\n", analyzerType);

        // Create an analyzer
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
            default:
                analyzer = null;
                System.out.println("Invalid analyzer type. Valid: StandardAnalyzer, SimpleAnalyzer, and EnglishAnalyzer.");
        }
        
        // Create a directory for the index
        Directory directory = FSDirectory.open(Paths.get(indexDirectory));
        
        // Configure the IndexWriter
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        // Create an IndexWriter
        IndexWriter writer = new IndexWriter(directory, config);
           
        // Index all Foreign Broadcast Information Service files
        File dir = new File(filePath);
        File[] fbsiFiles = dir.listFiles();
        if (fbsiFiles != null) {
            for (File fbsiFile : fbsiFiles) {
                System.out.println("Parsing document from file: " + fbsiFile.toString());
                List<Map<String, String>> fbisDocuments = parsefbsiDocument2(fbsiFile.toString());

                // Index the documents
                for (Map<String, String> document : fbisDocuments) {
                    indexDocument(writer, document);
                }
            }
        } else {
            System.out.println(String.format("FBSI directory: \"%s\" is not a directory", filePath.toString()));
            System.exit(1);
        }
        
        // Close the IndexWriter and Directory
        writer.close();
        directory.close();
    }


    // Parse fbsi Document
    private static List<Map<String, String>> parsefbsiDocument2(String filePath) throws IOException {
        List<String> documentLines = Files.readAllLines(Path.of(filePath), StandardCharsets.ISO_8859_1);
        List<Map<String, String>> documents = new ArrayList<>();
        Map<String, String> currentDocument = new HashMap<>();

        String currentField = null;
        StringBuilder currentValue = new StringBuilder();

        // String tags = "DOCNO|HT|HEADER|AU|DATE1|F P=100|F P=101|F P=102|F P=103|F P=104|TEXT";
        String tags = "DOCNO|DATE1|TEXT";
        boolean accumulateText = false;

        for (String line : documentLines) {
            if (line.matches("<(" + tags + ")>.*</(" + tags + ")>")) {
                // Both start and end tags are on the same line
                Pattern pattern = Pattern.compile("<(" + tags + ")>(.*?)</(" + tags + ")>");
                Matcher matcher = pattern.matcher(line);
        
                while (matcher.find()) {
                    currentField = matcher.group(1);
                    currentValue.append(matcher.group(2)).append(" ");
                    currentDocument.put(currentField, currentValue.toString().trim());
                    currentValue.setLength(0);
                }
            } else if (line.matches("<(" + tags + ")>.*")) {
                // Start tag found, accumulate text
                accumulateText = true;
                Pattern pattern = Pattern.compile("<(" + tags + ")>(.*?)$");
                Matcher matcher = pattern.matcher(line);
        
                while (matcher.find()) {
                    currentField = matcher.group(1);
                    currentValue.setLength(0);
                    currentValue.append(matcher.group(2)).append(" ");
                }
            } else if (line.matches(".*</(DOC|" + tags + ")>")) {
                // End tag found, stop accumulating text
                accumulateText = false;
                Pattern pattern = Pattern.compile("^(.*?)</(DOC|" + tags + ")>");
                Matcher matcher = pattern.matcher(line);
                
                while (matcher.find()) {
                    if (line.equals("</DOC>")) {
                        documents.add(currentDocument);
                        currentDocument = new HashMap<>();
                    } else {
                        currentValue.append(matcher.group(1)).append(" ");
                        currentDocument.put(currentField, currentValue.toString().trim());
                        currentValue.setLength(0);
                    }
                }
            } else if (accumulateText == true) {
                // Within a tag and line contains text, accumulate it
                currentValue.append(line).append(" ");
            }
        }
        return documents;
    }

    // Index a single document
    private static void indexDocument(IndexWriter writer, Map<String, String> documentFields) throws IOException {
        Document document = new Document();
        for (Map.Entry<String, String> entry : documentFields.entrySet()) {
            document.add(new TextField(entry.getKey(), entry.getValue().trim(), Field.Store.YES));
        }

        // Print output for testing
        System.out.println(String.format(
            "DOCNO => %s\n"
            + "HT => %s\n"
            + "HEADER => %s\n"
            + "AU => %s\n"
            + "DATE1 => %s\n"
            + "Region => %s\n"          // P=100
            + "Country => %s\n"         // P=101
            + "idAndLocation => %s\n"   // P=102
            + "id => %s\n"              // P=103
            + "Language => %s\n"        // P=104
            + "TEXT => %s\n",
            document.get("DOCNO"),
            document.get("HT"),
            document.get("HEADER"),
            document.get("AU"),
            document.get("DATE1"),
            document.get("F P=100"),
            document.get("F P=101"),
            document.get("F P=102"),
            document.get("F P=103"),
            document.get("F P=104"),
            document.get("TEXT")
        ));

        writer.addDocument(document);
    }

}
