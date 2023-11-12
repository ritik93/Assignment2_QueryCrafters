package org.querycrafters;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;

public class StandardDoc {
    Document doc;
    FieldType ft;

    public StandardDoc() {
        this.doc = new Document();
        this.ft = new FieldType(TextField.TYPE_STORED);
        ft.setTokenized(true);
        ft.setStoreTermVectors(true);
        ft.setStoreTermVectorPositions(true);
        ft.setStoreTermVectorOffsets(true);
        ft.setStoreTermVectorPayloads(true);
    }
    public void addDocNo(String Content){
        doc.add(new Field("DocNo", Content, ft));
    }
    public void addTitle(String Content){
        doc.add(new Field("Title", Content, ft));
    }
    public void addDate(String Content){
        doc.add(new Field("Date", Content, ft));
    }
    public void addAuthor(String Content){
        doc.add(new Field("Author", Content, ft));
    }
    public void addContent(String Content){
        doc.add(new Field("Content", Content, ft));
    }
    public void addSection(String Content){
        doc.add(new Field("Section", Content, ft));
    }
    public Document getDoc(){
        return doc;
    }

}
