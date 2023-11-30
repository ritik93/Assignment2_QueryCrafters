package org.querycrafters.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.querycrafters.constants;
import org.querycrafters.indexers.ft_indexer;

public class commonIndexer {
    static ArrayList<Document> alldocs = new ArrayList<>();
    static ft_indexer ft_in = new ft_indexer();
    //Declare indexer objects for all other documents similarly
    
    public static ArrayList<Document> data_indexer() {
        alldocs.addAll(ft_in.get_docs());
        return alldocs;
    }

    public static void Indexer(Analyzer analyzer, Similarity similarity) throws IOException {
        data_indexer();
        Directory dir;
        dir = FSDirectory.open(Paths.get(constants.Index_Location));

        //Configuring the Index Writer with the analyzer
        IndexWriterConfig ind_config = new IndexWriterConfig(analyzer);
        ind_config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        // Set the similarity score
        ind_config.setSimilarity(similarity);

        //Create the index writer with the configuration
        IndexWriter ind_writer = new IndexWriter(dir, ind_config);

        //Add the parsed documents to the index writer
        ind_writer.addDocuments(alldocs);
        ind_writer.close();

        System.out.println(String.valueOf(alldocs.size()) + " have been indexed \nIndexing complete");
    }

    public void gen_ind(Analyzer analyzer, Similarity similarity) throws IOException {
        Indexer(analyzer, similarity);
    }
}
