package org.querycrafters;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;

public class FRParser {
    private StandardAnalyzer analyzer;
    private Directory directory;

    public FRParser(StandardAnalyzer analyzer, String outputDir) throws IOException {
        this.analyzer = analyzer;
        this.directory = FSDirectory.open(Paths.get(outputDir));
    }

    public void index(File file) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(directory, config);
        StringBuilder currentString = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file.getPath()))) {
            String line;
            StandardDoc doc = new StandardDoc();
            boolean inDoc = false;
            boolean isText = false;
            boolean date = false;
            boolean content = false;
            StringBuilder currentString = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("<DOC>")) {
                    if (inDoc) {
                        writer.addDocument(doc.getDoc());
                        doc = new StandardDoc();
                    }
                    inDoc = true;
                } else if (line.startsWith("<DOCNO>")) {
                    docNo = true;
                    String docNoValue = line.replace("<DOCNO>", "").trim();
                    doc.addDocNo(docNoValue);
                } else if (docNo && line.startsWith("</DOCNO>")) {
                    docNo = false;
                } else if (line.startsWith("<TEXT>")) {
                    isText = true;
                } else if (line.startsWith("<DATE>")) {
                    date = true;
                } else if (date && isText) {
                    currentString.append(line).append(" ");
                } else if (line.startsWith("</DATE>")) {
                    date = false;
                    if (currentString.length() > 0) {
                        doc.addDate(currentString.toString());
                        currentString = new StringBuilder();
                    }
                } else if (line.startsWith("<TEXT>")) {
                    content = true;
                } else if (content && isText) {
                    currentString.append(line).append(" ");
                } else if (line.startsWith("</TEXT>")) {
                    content = false;
                    if (currentString.length() > 0) {
                        doc.addContent(currentString.toString());
                        currentString = new StringBuilder();
                    }
                }
            }

            if (inDoc) {
                writer.addDocument(doc.getDoc());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        public void shutdown() throws IOException {
        directory.close();
    }

    public static void main(String[] args) throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        String outputDir = "../Assignment2/index";
        FRParser parser = new FRParser(analyzer, outputDir);
        File folder = new File (System.getProperty("user.dir") + "/src/main/resources/Assignment Two/fr");
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
