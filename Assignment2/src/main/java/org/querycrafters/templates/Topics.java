package org.querycrafters.templates;

public class Topics {
    public static final String TOPIC_TAG = "top";
    public static final String TOPIC_START = "<top>";
    public static final String TOPIC_END = "</top>";
    public static final String TOPIC_NUM = "num";
    public static final String TOPIC_TITLE = "title";
    public static final String TOPIC_DESCRIPTION = "desc";
    public static final String TOPIC_NARRATIVE = "narr";

    private String topicNum;
    private String topicTitle;
    private String topicDesc;
    private String topicNarrative;

    public Topics(String topicNum, String topicTitle, String topicDesc, String topicNarrative) {
        this.topicNum = topicNum;
        this.topicTitle = topicTitle;
        this.topicDesc = topicDesc;
        this.topicNarrative = topicNarrative;
    }

    public String getTopicNum() {
        return topicNum;
    }

    public void setTopicNum(String topicNum) {
        this.topicNum = topicNum;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public String getTopicDesc() {
        return topicDesc;
    }

    public void setTopicDesc(String topicDesc) {
        this.topicDesc = topicDesc;
    }

    public String getTopicNarrative() {
        return topicNarrative;
    }

    public void setTopicNarrative(String topicNarrative) {
        this.topicNarrative = topicNarrative;
    }

    @Override
    public String toString() {
        return "topics{" +
                "topicNum='" + topicNum + '\'' +
                ", topicTitle='" + topicTitle + '\'' +
                ", topicDesc='" + topicDesc + '\'' +
                ", topicNarrative='" + topicNarrative + '\'' +
                '}';
    }
}
