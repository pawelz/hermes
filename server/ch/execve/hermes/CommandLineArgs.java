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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/** A JCommander configuration for command line arguments. */
public class CommandLineArgs {
    public static CommandLineArgs getFlags(String[] args) {
        var flags = new CommandLineArgs();
        JCommander.newBuilder()
            .addObject(flags)
            .build()
            .parse(args);
        return flags;
    }

    @Parameter(names = "--config-dir", description = "Base directory for configuration files")
    private String configDir = ".";

    @Parameter(names = "--socket-path", description = "Path for the Unix domain socket")
    private String socketPath = "hermes.sock";

    @Parameter(names = "--log-file", description = "Path to log file. If not specified, logs to stdout.")
    private String logFile = null;

    public String getConfigDir() {
        return configDir;
    }

    public String getSocketPath() {
        return socketPath;
    }

    public String getLogFile() {
        return logFile;
    }

    private CommandLineArgs() {}
}