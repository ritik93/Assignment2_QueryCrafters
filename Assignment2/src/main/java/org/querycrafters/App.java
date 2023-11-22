package org.querycrafters;

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
