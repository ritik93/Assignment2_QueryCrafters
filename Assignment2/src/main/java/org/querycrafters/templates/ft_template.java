package org.querycrafters.templates;

public class ft_template {
    String docno, date, headline, byline, text;
    
    public String getDocNo(){
        return this.docno;
    }

    public void setDocNo(String docno){
        this.docno = docno;
    }

    public String getDate(){
        return this.date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getHeadline(){
        return this.headline;
    }

    public void setHeadline(String headline){
        this.headline = headline;
    }

    public String getByline(){
        return this.byline;
    }

    public void setByline(String byline){
        this.byline = byline;
    }
    
    public String getText(){
        return this.text;
    }

    public void setText(String text){
        this.text = text;
    }
}
