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

public class Fr94Parser {
    private Analyzer analyzer;
    private Directory directory;

    public Fr94Parser(Analyzer analyzer, String outputDir) throws IOException {
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

        String tags = "DOCNO|TEXT|DATE";
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
                } else if (accumulateText) {
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

    public static String removeHTMLComments(String input) {
        StringBuilder output = new StringBuilder();

        int startIndex, endIndex;
        while ((startIndex = input.indexOf("<!--")) != -1) {
            endIndex = input.indexOf("-->", startIndex);
            if (endIndex == -1) {
                break;
            }
            output.append(input, 0, startIndex);
            input = input.substring(endIndex + 3);
        }
        output.append(input);

        return output.toString();
    }

    private void addToDoc(StandardDoc doc, String currentField, String currentValue) throws IOException {
        String newCurrentValue = null;
        switch (currentField) {
            case "DOCNO":
                newCurrentValue = removeHTMLComments(currentValue);
                doc.addDocNo(newCurrentValue);
                break;
            case "TEXT":
                newCurrentValue = removeHTMLComments(currentValue);
                doc.addContent(newCurrentValue);
                break;
            case "DATE":
                newCurrentValue = removeHTMLComments(currentValue);
                doc.addDate(newCurrentValue);
                break;
            default:
                //System.out.print(String.format("Unknown field parsed: %s\n => %s\n\n", currentField, currentValue));
        }
    }


    public void indexFr94() throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        String outputDir = "../Assignment2/index";
        Fr94Parser parser = new Fr94Parser(analyzer, outputDir);
        File rootFolder = new File(System.getProperty("user.dir") + "/src/main/resources/Assignment Two/fr94");
        processFolder(rootFolder, parser);
        parser.shutdown();
    }

    public static void main(String[] args) throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        String outputDir = "../Assignment2/index";
        Fr94Parser parser = new Fr94Parser(analyzer, outputDir);
        File rootFolder = new File(System.getProperty("user.dir") + "/src/main/resources/Assignment Two/fr94");
        processFolder(rootFolder, parser);
        parser.shutdown();
    }

    private static void processFolder(File folder, Fr94Parser parser) {
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("fr94");
            }
        });

        if (files != null) {
            for (File file : files) {
                try {
                    parser.index(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }            
            }
        }

        File[] subFolders = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        if (subFolders != null) {
            for (File subFolder : subFolders) {
                processFolder(subFolder, parser);
            }
        }
    }

}
