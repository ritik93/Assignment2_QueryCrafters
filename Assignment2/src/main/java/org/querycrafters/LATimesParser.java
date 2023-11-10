package org.querycrafters;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
        try (BufferedReader reader = new BufferedReader(new FileReader(file.getPath()))) {
            String line;
            while ((line = reader.readLine()) != null) {

            }
        }
    }
    public void shutdown() throws IOException {
        directory.close();
    }

    public static void main(String[] args) throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        String outputDir = "../index";
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
