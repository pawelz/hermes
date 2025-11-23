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

import ch.execve.hermes.classifier.Classifier;
import ch.execve.hermes.classifier.HeaderMatcher;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.MessagingException;
import jakarta.mail.Message;

/** Dispatches email to Classifiers. */
class Dispatcher {
    ImmutableMap<Classifier, String> classifiers;
    @Inject
    Dispatcher(@Named("rulesDir") String rulesDir) {
        this.classifiers = ImmutableMap.<Classifier, String>builder()
            .put(new HeaderMatcher(rulesDir + "/spam.json"), "spam/garbage")
            .build();
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
            System.out.println(
                String.format(
                    "Classified message from '%s', subject '%s' as '%s'", 
                    message.getFrom()[0],
                    message.getSubject(),
                    messageClass));
            return messageClass;
        } catch (MessagingException e) {
            System.err.println("Failed to read message properties: " + e.getMessage());
            return "INBOX.hermes-error";
        }
    }
}
