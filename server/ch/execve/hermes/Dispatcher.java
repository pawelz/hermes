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
import jakarta.mail.MessagingException;
import jakarta.mail.Message;

/** Dispatches email to Classifiers. */
class Dispatcher {
    ImmutableMap<Classifier, String> classifiers;

    Dispatcher() {
        this.classifiers = ImmutableMap.of();
    }

    String dispatch(Message message) {
        try {
            // For now, let's just return the subject as a proof of concept.
            String subject = message.getSubject();
            System.out.println("Parsed email with subject: " + subject);
            return "INBOX";
        } catch (MessagingException e) {
            // This is less likely to happen here now, but good to keep.
            System.err.println("Failed to read message properties: " + e.getMessage());
            return "INBOX.hermes-error";
        }
    }
}
