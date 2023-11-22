package org.querycrafters.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.querycrafters.templates.Topics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicsParser {
        private static List<Topics> topicsList;

        public static List<Topics> parse(String topicsFilePath) throws IOException {
            topicsList = new ArrayList<>();

            // Parse the HTML document using Jsoup
            Document jsoupDoc = Jsoup.parse(new File(topicsFilePath), "UTF-8", "");

            /*
                // Without disabling pretty print
                Document documentWithPrettyPrint = Jsoup.parse("<html><head></head><body><p>Hello</p></body></html>");
                System.out.println(documentWithPrettyPrint);

               // With disabling pretty print
                Document documentWithoutPrettyPrint = Jsoup.parse("<html><head></head><body><p>Hello</p></body></html>");
                documentWithoutPrettyPrint.outputSettings(new Document.OutputSettings().prettyPrint(false));
                System.out.println(documentWithoutPrettyPrint);
             */

            jsoupDoc.outputSettings(new Document.OutputSettings().prettyPrint(false));
            Elements topics = jsoupDoc.select(Topics.TOPIC_TAG);

            for (Element topicElement : topics) {
                // Extracting topic number, title, description, and narrative
                String num = topicElement.getElementsByTag(Topics.TOPIC_NUM).text();
                String title = topicElement.getElementsByTag(Topics.TOPIC_TITLE).text();
                String descStr = topicElement.getElementsByTag(Topics.TOPIC_DESCRIPTION).text();
                String narrativeStr = topicElement.getElementsByTag(Topics.TOPIC_NARRATIVE).text();

                // Extracting number using regular expression
                Pattern numberPattern = Pattern.compile("(\\d+)");
                Matcher numberMatcher = numberPattern.matcher(num);
                String number = "";
                if (numberMatcher.find()) {
                    number = numberMatcher.group().trim();
                }

                // extracting description using regular expression
                descStr = descStr.replace("\n", " ");
                Pattern descPattern = Pattern.compile("Description: (.*)Narrative");
                Matcher descMatcher = descPattern.matcher(descStr);
                String desc = "";
                if (descMatcher.find()) {
                    desc = descMatcher.group(1).trim();
                }

                // Cleaning up narrative
                String narrative = narrativeStr.replace("\n", " ").replace("Narrative: ", "").trim();

                // Creating a TopicsModel object and adding it to the list
                Topics topic = new Topics(number, title, desc, narrative);
                topicsList.add(topic);
            }

            // Return the list of parsed topics
            return topicsList;
        }

    public static void main(String[] args) {
        String topicsFilePath = "Documents/topics";

        try {
            List<Topics> topics = TopicsParser.parse(topicsFilePath);
            for (Topics topic : topics) {
                System.out.println(topic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

