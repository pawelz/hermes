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

#include <stdio.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char **argv) {
    if (argc != 2) {
        printf("Usage: %s socket\n", argv[0]);
        return 1;
    }

    char *socket_path = argv[1];

    int sockfd;
    struct sockaddr_un addr;
    sockfd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sockfd == -1) {
        fprintf(stderr, "socket error (%s): ", socket_path);
        perror(NULL);
        return 1;
    }

    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, socket_path, sizeof(addr.sun_path) - 1);

    if (connect(sockfd, (struct sockaddr*)&addr, sizeof(addr)) == -1) {
        fprintf(stderr, "connect error (%s): ", socket_path);
        perror(NULL);
        return 1;
    }

    char *buffer = NULL;
    size_t buffer_size = 0;
    ssize_t bytes_read;

    while ((bytes_read = getline(&buffer, &buffer_size, stdin)) != -1) {
        if (send(sockfd, buffer, bytes_read, 0) == -1) {
            perror("send error");
        }
        free(buffer);
        buffer = NULL;
    }

    // shutdown closes the "write" part of the connection, so that the server
    // knows that we are done and can send us a response.
    shutdown(sockfd, SHUT_WR);

    char response_buffer[1024];
    ssize_t response_bytes = recv(sockfd, response_buffer, sizeof(response_buffer) - 1, 0);
    if (response_bytes == -1) {
        perror("recv error");
    } else if (response_bytes > 0) {
        response_buffer[response_bytes] = '\0'; // Null-terminate the received data
        puts(response_buffer);
    }

    close(sockfd);
    free(buffer);

    return 0;
}