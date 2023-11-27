package org.querycrafters;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TopicParser {
        public List<String> getQueries() throws FileNotFoundException {
            List<String> queries = new ArrayList<>();
            String filename = System.getProperty("user.dir") + "/src/main/resources/topics";
            boolean isDescription = false;
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                StringBuilder currentString = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("<desc> Description: ")) {
                        isDescription = true;
                    }
                    else if (line.startsWith("<")) {
                        isDescription = false;
                        if (currentString.length() > 0) {
                            queries.add(currentString.toString().trim());
                        }
                    }
                    else if (isDescription) {
                        currentString.append(line).append(" ");
                    }
                }
                // Add the last string if there's any left
                if (currentString.length() > 0) {
                    queries.add(currentString.toString().trim());
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
            }
            return queries;
        }

}
