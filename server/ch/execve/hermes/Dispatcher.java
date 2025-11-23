// Copyright 2025 Paweł Zuzelski <pawelz@execve.ch>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ch.execve.hermes;

import com.google.common.collect.ImmutableMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ch.execve.hermes.classifier.Classifier;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.MessagingException;
import jakarta.mail.Message;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Dispatches email to Classifiers. */
class Dispatcher {
    private final ImmutableMap<Classifier, String> classifiers;
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    @Inject
    Dispatcher(@Named("configDir") String configDir) {
        var builder = ImmutableMap.<Classifier, String>builder();
        var mapper = new ObjectMapper();
        var configFile = new File(configDir, "classifiers.json");

        try {
            List<ClassifierConfig> configs = mapper.readValue(configFile, new TypeReference<List<ClassifierConfig>>() {});
            for (var config : configs) {
                logger.info("Loading classifier: {}", config.name());
                // Use reflection to instantiate the specified classifier class
                var implementation = Class.forName(config.implementation());
                var constructor = implementation.getConstructor(String.class);
                var classifierPath = new File(configDir, config.name() + ".json").getAbsolutePath();
                var classifierInstance = (Classifier) constructor.newInstance(classifierPath);
                builder.put(classifierInstance, config.destination());
            }
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to load and instantiate classifiers from " + configFile.getAbsolutePath(), e);
        }

        this.classifiers = builder.build();
    }

    String dispatch(Message message) {
        try {
            var messageClass = classifiers
                .keySet()
                .stream()
                .filter(c -> c.classify(message))
                .findFirst()
                .map(classifiers::get)
                .orElse("INBOX");
            logger.info(
                "Classified message from '{}', subject '{}' as '{}'",
                message.getFrom()[0],
                message.getSubject(),
                messageClass);
            return messageClass;
        } catch (MessagingException e) {
            logger.error("Failed to read message properties", e);
            return "INBOX.hermes-error";
        }
    }
}
