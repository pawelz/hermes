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

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Guice module for configuring Hermes application bindings.
 */
public class HermesModule extends AbstractModule {
    private final CommandLineArgs args;

    public HermesModule(CommandLineArgs args) {
        this.args = args;
    }

    @Override
    protected void configure() {
        // Bind the values from the command line flags to named annotations.
        bind(String.class).annotatedWith(Names.named("socketPath")).toInstance(args.getSocketPath());
        bind(String.class).annotatedWith(Names.named("configDir")).toInstance(args.getConfigDir());
        bind(String.class).annotatedWith(Names.named("databasePath")).toInstance(args.getDatabasePath());
    }
}