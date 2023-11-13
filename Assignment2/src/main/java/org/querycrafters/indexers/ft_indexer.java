package org.querycrafters.indexers;

import java.util.ArrayList;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import org.querycrafters.templates.ft_template;
import org.querycrafters.parsers.ft_parser;

public class ft_indexer {
    private Document create_doc(ft_template data) {
        Document doc = new Document();

        doc.add(new StringField("docno", data.getDocNo(), Field.Store.YES));
        doc.add(new TextField("date", data.getDate(), Field.Store.YES));
        doc.add(new TextField("headline", data.getHeadline(), Field.Store.YES));
        doc.add(new TextField("author", data.getByline(), Field.Store.YES));
        doc.add(new TextField("content", data.getText(), Field.Store.YES));

        return doc;
    }

    private static ArrayList<Document> ft_doc_list = new ArrayList<>();
    
    public ArrayList<Document> get_docs() {
        ft_parser data = new ft_parser();
        ArrayList<ft_template> parsed_data;

        try {
            parsed_data = data.parsedata();

            for (int i = 0; i < parsed_data.size(); i++) {
                ft_doc_list.add(create_doc(parsed_data.get(i)));
            }
        } catch (Exception e) {
        }

        return ft_doc_list;
    }
}
