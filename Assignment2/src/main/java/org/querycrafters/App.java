package org.querycrafters;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;

import org.querycrafters.FBSIParser;
import org.querycrafters.LATimesParser;

public class App 
{
    public static void main(String[] args) throws IOException {
        // example calls:
        // java -jar target/Assignment2-0.1.jar StandardAnalyzer
        // java -jar target/Assignment2-0.1.jar SimpleAnalyzer
        // java -jar target/Assignment2-0.1.jar EnglishAnalyzer
        // java -jar target/Assignment2-0.1.jar EnglishAnalyzer-getDefaultStopSet
        if (args.length < 1) {
            System.out.println("Expected arguments: <analyzerType>");
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
            default:
                analyzer = null;
                System.out.println("Invalid analyzer type. Valid: StandardAnalyzer, SimpleAnalyzer, and EnglishAnalyzer.");
        }

        System.out.println("Indexing Foreign Broadcast Information Service");
        FBSIParser FBSIParser = new FBSIParser(analyzer, outputDir);
        File FBSIfolder = new File (System.getProperty("user.dir") + "/src/main/resources/Assignment Two/fbis");
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
        File LATimesfolder = new File (System.getProperty("user.dir") + "/src/main/resources/Assignment Two/latimes");
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
    
        // Todo
        System.out.println("Indexing FR routing data");

        // Todo
        System.out.println("Indexing Financial Times");
    }
}
