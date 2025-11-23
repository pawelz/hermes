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
import java.io.IOException;
import java.util.Arrays;

public class HermesServer {

    public static void main(String[] args) throws IOException {
        // 1. Define and parse all command line arguments.
        CommandLineArgs cmdArgs = new CommandLineArgs();
        JCommander.newBuilder()
            .addObject(cmdArgs)
            .build()
            .parse(args);
        
        System.out.println("Using socket path: " + cmdArgs.getSocketPath());
        System.out.println("Using rules directory: " + cmdArgs.getRulesDir());

        // 2. Use the parsed arguments to create the application components.
        // The rules directory is passed to the Dispatcher.
        Dispatcher dispatcher = new Dispatcher(cmdArgs.getRulesDir());
        // 3. The socket path and the configured dispatcher are passed to the SocketListener.
        SocketListener socketListener = new SocketListener(cmdArgs.getSocketPath(), dispatcher);
        socketListener.start();
    }
}
