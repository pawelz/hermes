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

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class SocketListener {

    private static final int BUFFER_SIZE = 4096;
    private final String socketPath;
    private final Dispatcher dispatcher;

    public SocketListener(String socketPath) {
        this.socketPath = socketPath;
        this.dispatcher = new Dispatcher();
    }

    /** Starts the service listening on the Unix socket. */
    void start() {
        Path socket = Path.of(socketPath);

        try {
            Files.deleteIfExists(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socket);

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            serverChannel.bind(address);
            System.out.println("Hermes Server listening on: " + socket);

            while (true) {
                // Blocks until a client (the Go binary) connects
                try (SocketChannel clientChannel = serverChannel.accept()) {
                    System.out.println("Client connected. Processing email...");
                    handleClient(clientChannel);
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }

        } catch (Throwable e) {
            System.err.println("Failed to start server: " + e.getMessage());
            // Re-create the socket file to ensure correct permissions/ownership if needed for production
            try {
                Files.deleteIfExists(socket);
            } catch (IOException f) {
                throw new RuntimeException(f);
            }
        }
    }

    void handleClient(SocketChannel clientChannel) throws IOException {
        // Use a StringBuilder to collect the entire email content
        StringBuilder emailContent = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        
        // 1. Read the incoming data stream until EOF (Go client calls CloseWrite())
        while (clientChannel.read(buffer) > 0) {
            buffer.flip(); // Prepare buffer for reading
            emailContent.append(new String(buffer.array(), 0, buffer.limit()));
            buffer.clear(); // Prepare buffer for writing
        }

        String rawEmail = emailContent.toString();
        
        // Just a placeholder while I am figuring out the connectivity part.
        System.out.println("Received:\n" + rawEmail + "\n");
        String response = dispatcher.dispatch(rawEmail);

        ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());

        clientChannel.write(responseBuffer);
    }
}
