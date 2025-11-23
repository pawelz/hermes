package ch.execve.hermes.classifier;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import jakarta.mail.Message;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class HeaderMatcher implements Classifier {

    private final ImmutableList<Rule> rules;

    private record Rule(String header, List<String> regex) {}

    public HeaderMatcher(String rulesPath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get(rulesPath));
            this.rules = ImmutableList.copyOf(mapper.readValue(jsonData, new TypeReference<List<Rule>>() {}));
            System.out.println("Successfully loaded " + this.rules.size() + " rules from JSON.");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read or parse JSON rules file: " + rulesPath, e);
        }
    }

    @Override
    public boolean classify(Message email) {
        for (Rule rule : rules) {
            String[] headerValues;
            try {
                headerValues = email.getHeader(rule.header());
            } catch (jakarta.mail.MessagingException e) {
                // TODO: error logging
                continue; // Skip this rule if header can't be read
            }
            if (headerValues == null) {
                continue;
            }
            for (String re : rule.regex()) {
                for (String headerValue : headerValues) {
                    if (headerValue.matches(re)) return true;
                }
            }
        }
        return false;
    }
}
