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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class SocketListener {

    private static final Logger logger = LoggerFactory.getLogger(SocketListener.class);
    private static final int BUFFER_SIZE = 4096;
    private final String socketPath;
    private final Dispatcher dispatcher;
    private final Session session;
    
    @Inject
    public SocketListener(@Named("socketPath") String socketPath, Dispatcher dispatcher) {
        this.socketPath = socketPath;
        this.dispatcher = dispatcher;
        this.session = Session.getDefaultInstance(new Properties());
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
            logger.info("Hermes Server listening on: {}", socket);

            while (true) {
                // Blocks until a client (the Go binary) connects
                try (SocketChannel clientChannel = serverChannel.accept()) {
                    logger.info("Client connected. Processing email...");
                    handleClient(clientChannel);
                } catch (IOException e) {
                    logger.error("Error handling client connection", e);
                }
            }

        } catch (Throwable e) {
            logger.error("Failed to start server", e);
            // Re-create the socket file to ensure correct permissions/ownership if needed for production
            try {
                Files.deleteIfExists(socket);
            } catch (IOException f) {
                throw new RuntimeException(f);
            }
        }
    }

    void handleClient(SocketChannel clientChannel) throws IOException {
        // Use a ByteArrayOutputStream to collect the raw bytes of the email
        ByteArrayOutputStream emailBytes = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        
        // 1. Read the incoming data stream until EOF (client calls shutdown(SHUT_WR))
        while (clientChannel.read(buffer) > 0) {
            buffer.flip(); // Prepare buffer for reading
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            emailBytes.write(data);
            buffer.clear(); // Prepare buffer for writing
        }

        String response;
        try (InputStream emailStream = new ByteArrayInputStream(emailBytes.toByteArray())) {
            Message message = new MimeMessage(session, emailStream);
            response = dispatcher.dispatch(message);
        } catch (MessagingException e) {
            logger.error("Failed to parse email", e);
            response = "INBOX.hermes-error";
        }

        ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));

        clientChannel.write(responseBuffer);
    }
}
