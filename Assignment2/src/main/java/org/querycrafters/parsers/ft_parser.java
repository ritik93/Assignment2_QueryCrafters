package org.querycrafters.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.querycrafters.templates.ft_template;

public class ft_parser {
    private final static File ft_directory = new File("Documents/ft");

    private static ArrayList<ft_template> ft_list = new ArrayList<>();

    private static void parsesinglefile(String document_to_parse) throws IOException{
        ft_template ft_template = null;
        File file = new File(document_to_parse);
        Document doc = Jsoup.parse(file, "UTF-8", "https://example.com");

        for (Element ele : doc.select("DOC")) {
            ft_template = new ft_template();
            ft_template.setDocNo(ele.select("DOCNO").text());
            ft_template.setDate(ele.select("DATE").text());
            ft_template.setHeadline(ele.select("HEADLINE").text());
            ft_template.setByline(ele.select("BYLINE").text());
            ft_template.setText(ele.select("TEXT").text());

            ft_list.add(ft_template);
        }
    }

    public static void parseall(String path) throws IOException{
        File base = new File(path);
        File[] file_list = base.listFiles();

        if (file_list != null) {
            for (File file : file_list) {
                if (file.isDirectory()) {
                    parseall(file.getAbsolutePath());
                }

                else {
                    if (!file.getName().contains("read") && !file.getName().contains("Zone.Identifier")) {
                        parsesinglefile(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    public ArrayList<ft_template> parsedata() throws IOException {
        parseall(ft_directory.getAbsolutePath());
        return ft_list;
    }
}
