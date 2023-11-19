package org.querycrafters;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FBSIParser {
    private Analyzer analyzer;
    private Directory directory;

    public FBSIParser(Analyzer analyzer, String outputDir) throws IOException {
        this.analyzer = analyzer;
        this.directory = FSDirectory.open(Paths.get(outputDir));
    }

    public void index(File file) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(directory, config);


        StandardDoc doc = new StandardDoc();
        boolean inDoc = false;
        boolean date = false;
        boolean content = false;
        boolean isText = false;
        StringBuilder currentString = new StringBuilder();

        //List<String> documentLines = Files.readAllLines(Path.of(directory), StandardCharsets.ISO_8859_1);
        List<String> documentLines = Files.readAllLines(file.toPath(), StandardCharsets.ISO_8859_1);
        List<Map<String, String>> documents = new ArrayList<>();
        Map<String, String> currentDocument = new HashMap<>();

        String currentField = null;
        StringBuilder currentValue = new StringBuilder();

        // String tags = "DOCNO|HT|HEADER|AU|DATE1|F P=100|F P=101|F P=102|F P=103|F P=104|TEXT";
        String tags = "DOCNO|DATE1|TEXT";
        boolean accumulateText = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file.getPath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches("<(" + tags + ")>.*</(" + tags + ")>")) {
                // Both start and end tags are on the same line
                Pattern pattern = Pattern.compile("<(" + tags + ")>(.*?)</(" + tags + ")>");
                Matcher matcher = pattern.matcher(line);
        
                while (matcher.find()) {
                    currentField = matcher.group(1);
                    currentValue.append(matcher.group(2)).append(" ");
                    currentDocument.put(currentField, currentValue.toString().trim());
                    // System.out.print(String.format("%s\n => %s\n\n",currentField, currentValue.toString()));
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
                        // System.out.print("======================================================================================\n");
                        documents.add(currentDocument);
                        currentDocument = new HashMap<>();
                    } else {
                        currentValue.append(matcher.group(1)).append(" ");
                        currentDocument.put(currentField, currentValue.toString().trim());
                        // System.out.print(String.format("%s\n => %s\n\n",currentField, currentValue.toString()));
                        currentValue.setLength(0);
                    }
                }
            } else if (accumulateText == true) {
                // Within a tag and line contains text, accumulate it
                currentValue.append(line).append(" ");
            }
            }
        }
        writer.close();
    }
    public void shutdown() throws IOException {
        directory.close();
    }

    public static void main(String[] args) throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        String outputDir = "../Assignment2/index";
        FBSIParser parser = new FBSIParser(analyzer, outputDir);
        File folder = new File (System.getProperty("user.dir") + "/src/main/resources/Assignment Two/fbis");
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("fb");
            }
        });
        for (File file : files) {
            parser.index(file);
        }
        parser.shutdown();
    }
}
