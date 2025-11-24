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
#include <unistd.h>
#include <string.h>
#include <sys/time.h>

#include "libmaildir.h"


int main(int argc, char **argv) {
    if (argc != 3) {
        printf("Usage: %s socket maildir\n", argv[0]);
        return 1;
    }

    char *socket_path = argv[1];
    char *maildir_path = argv[2];

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

    // Set a 3-second timeout for receiving data. This is just not to avoid
    // deadlocks or otherwise the Java code getting stuck.
    struct timeval tv;
    tv.tv_sec = 3;
    tv.tv_usec = 0;
    if (setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof(tv)) < 0) {
        perror("setsockopt failed");
        close(sockfd);
        return 1;
    }

    // 1. Read the entire email from stdin into a buffer.
    char *email_buffer = NULL;
    size_t email_buffer_size = 0;
    FILE *email_stream = open_memstream(&email_buffer, &email_buffer_size);
    if (email_stream == NULL) {
        perror("open_memstream failed");
        return 1;
    }

    char *line_buffer = NULL;
    size_t line_buffer_size = 0;
    ssize_t bytes_read;
    while ((bytes_read = getline(&line_buffer, &line_buffer_size, stdin)) != -1) {
        fwrite(line_buffer, 1, bytes_read, email_stream);
    }
    fclose(email_stream);
    free(line_buffer);

    // 2. Send the buffered email to the server.
    if (send(sockfd, email_buffer, email_buffer_size, 0) == -1) {
        perror("send error");
        close(sockfd);
        free(email_buffer);
        return 1;
    }

    // 3. Shutdown the "write" part of the connection.
    shutdown(sockfd, SHUT_WR);

    // 4. Receive the destination path from the server.
    char response_buffer[1024];
    ssize_t response_bytes = recv(sockfd, response_buffer, sizeof(response_buffer) - 1, 0);
    if (response_bytes == -1) {
        perror("recv error");
    } else if (response_bytes > 0) {
        response_buffer[response_bytes] = '\0';

        size_t full_path_size = strlen(maildir_path) + 1 + strlen(response_buffer) + 1;
        char *full_maildir_path = malloc(full_path_size);
        if (full_maildir_path == NULL) {
            perror("malloc for full_maildir_path failed");
            return 1;
        }

        snprintf(full_maildir_path, full_path_size, "%s/%s", maildir_path, response_buffer);

        if (deliver_to_maildir(full_maildir_path, email_buffer, email_buffer_size) != 0) {
            fprintf(stderr, "maildir_deliver failed for path: %s\n", full_maildir_path);
            free(full_maildir_path);
            return 1;
        } else {
            printf("Delivered to: %s\n", full_maildir_path);
        }

        free(full_maildir_path);
    }

    close(sockfd);
    free(email_buffer);

    return 0;
}