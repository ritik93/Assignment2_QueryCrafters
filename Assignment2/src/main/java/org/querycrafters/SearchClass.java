package org.querycrafters;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class SearchClass {

    private Analyzer analyzer;
    private Directory directory;

    private Similarity similarity;
    public int queryNo = 1;

    public SearchClass(Analyzer analyzer, String INDEX_DIRECTORY, Similarity similarity) throws IOException {
        this.analyzer = analyzer;
        this.directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        this.similarity = similarity;
    }

    public void Search(String field, String queryString) throws IOException, ParseException {

        QueryParser parser = new QueryParser(field, analyzer);
        parser.setAllowLeadingWildcard(true);
        Query query = parser.parse(queryString);

        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);
        ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("temp.txt", true))) {
            int rank = 1;
            for (ScoreDoc scores : hits) {
                Document doc = searcher.doc(scores.doc);
                System.out.println("Result " + rank + ": " +doc.get("title"));
                String line = String.format("%d Q0 %d %d %f %s\n", queryNo, scores.doc, rank, scores.score, "Results");
                writer.write(line);
                rank++;
            }
        }
        queryNo++;
    }

}
