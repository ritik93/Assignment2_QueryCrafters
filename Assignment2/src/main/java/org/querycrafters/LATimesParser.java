package org.querycrafters;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;

public class LATimesParser {
    private StandardAnalyzer analyzer;
    private Directory directory;

    public LATimesParser(StandardAnalyzer analyzer, String outputDir) throws IOException {
        this.analyzer = analyzer;
        this.directory = FSDirectory.open(Paths.get(outputDir));
    }

    public void index(File file) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(directory, config);

        StandardDoc doc = new StandardDoc();
        boolean inDoc = false;
        boolean date = false;
        boolean title = false;
        boolean author = false;
        boolean content = false;
        boolean isText = false;
        StringBuilder currentString = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file.getPath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.startsWith("<DOCNO>")) {
                    if(inDoc) {
                        writer.addDocument(doc.getDoc());
                        doc = new StandardDoc();
                    }
                    inDoc = true;
                    String docNo = line.replace("<DOCNO>", "");
                    doc.addDocNo(docNo);
                }
                if(inDoc){
                    if(line.startsWith("<P>")){
                        isText = true;
                    }
                    else if(line.startsWith("</P>")){
                        isText = false;
                    }
                    else if(line.startsWith("<DATE>")) {
                        date = true;
                    }
                    else if(date && isText){
                        currentString.append(line).append(" ");
                    }
                    else if (line.startsWith("</DATE>")) {
                        date = false;
                        if (currentString.length() > 0) {
                            doc.addDate(currentString.toString());
                            currentString = new StringBuilder();
                        }
                    }
                    else if(line.startsWith("<HEADLINE>")) {
                        title = true;
                    }
                    else if(title && isText){
                        currentString.append(line).append(" ");
                    }
                    else if (line.startsWith("</HEADLINE>")) {
                        title = false;
                        if (currentString.length() > 0) {
                            doc.addTitle(currentString.toString());
                            currentString = new StringBuilder();
                        }
                    }
                    else if(line.startsWith("<BYLINE>")) {
                        author = true;
                    }
                    else if(author && isText){
                        currentString.append(line).append(" ");
                    }
                    else if (line.startsWith("</BYLINE>")) {
                        author = false;
                        if (currentString.length() > 0) {
                            doc.addAuthor(currentString.toString());
                            currentString = new StringBuilder();
                        }
                    }
                    else if(line.startsWith("<TEXT>")) {
                        content = true;
                    }
                    else if(content && isText){
                        currentString.append(line).append(" ");
                    }
                    else if (line.startsWith("</TEXT>")) {
                        content = false;
                        if (currentString.length() > 0) {
                            doc.addContent(currentString.toString());
                            currentString = new StringBuilder();
                        }
                    }
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
        LATimesParser parser = new LATimesParser(analyzer, outputDir);
        File folder = new File (System.getProperty("user.dir") + "/src/main/resources/Assignment Two/latimes");
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("la");
            }
        });
        for (File file : files) {
            parser.index(file);
        }
        parser.shutdown();
    }
}
