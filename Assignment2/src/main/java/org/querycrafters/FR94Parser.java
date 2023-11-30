package org.querycrafters;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FR94Parser {
    private Analyzer analyzer;
    private Directory directory;

    public FR94Parser(Analyzer analyzer, String outputDir) throws IOException {
        this.analyzer = analyzer;
        this.directory = FSDirectory.open(Paths.get(outputDir));
    }

    public void index(File file) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(directory, config);

        StandardDoc doc = new StandardDoc();

        String currentField = null;
        StringBuilder currentValue = new StringBuilder();

        // String tags = "DOCNO|HT|HEADER|AU|DATE1|F P=100|F P=101|F P=102|F P=103|F P=104|TEXT";
        String tags = "DOCNO|TEXT";
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
                    addToDoc(doc, currentField, currentValue.toString().trim());
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
                        writer.addDocument(doc.getDoc());
                        doc = new StandardDoc();
                    } else {
                        currentValue.append(matcher.group(1)).append(" ");
                        addToDoc(doc, currentField, currentValue.toString().trim());
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

    private void addToDoc(StandardDoc doc, String currentField, String currentValue) throws IOException {
        switch (currentField) {
            case "DOCNO":
                doc.addDocNo(currentValue);
                System.out.println(String.format("%s\n => %s\n\n",currentField, currentValue.toString()));
                break;
            case "TEXT":
                doc.addContent(currentValue);
                System.out.println(String.format("%s\n => %s\n\n",currentField, currentValue.toString()));
                break;
            default:
                System.out.print(String.format("Unkown field parsed: %s\n => %s\n\n",currentField, currentValue.toString()));
        }
    }

    public static void main(String[] args) throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        String outputDir = "../Assignment2/index";
        FR94Parser parser = new FR94Parser(analyzer, outputDir);
        File folder = new File (System.getProperty("user.dir") + "/src/main/resources/Assignment Two/fr94/01");
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("fr");
            }
        });
        for (File file : files) {
            parser.index(file);
        }
        parser.shutdown();
    }
}
