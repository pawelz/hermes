#!/bin/sh

# Copyright 2025 Paweł Zuzelski <pawelz@execve.ch>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -e

echo "--- Starting Hermes Test Runner ---"

# Bazel sets TEST_TMPDIR for us to use as a scratch directory.
TMPDIR="$TEST_TMPDIR"

# Use a relative file name for the socket to avoid path length limits.
SOCKET_PATH="hermes.sock"
MAILDIR_PATH="$TMPDIR/maildir"
DB_PATH="$TMPDIR/hermes.db"
LOG_PATH="$TMPDIR/hermes.log"

# The config files are provided as a data dependency.
CONFIG_DIR="tests/config"

# Start the Hermes server binary in the background.
echo "Starting server..."
./server/ch/execve/hermes/hermes_server \
  --socket-path "$SOCKET_PATH" \
  --database-path "$DB_PATH" \
  --log-file "$LOG_PATH" \
  --config-dir "$CONFIG_DIR" &

SERVER_PID=$!

# Clean up the server process on exit.
trap 'kill $SERVER_PID' EXIT

# Wait for the server to create the socket.
echo "Waiting for socket at $SOCKET_PATH..."
while [ ! -S "$SOCKET_PATH" ]; do
  sleep 0.1
done
echo "Server started successfully."

run_test() {
  email_file=$1
  expected_mailbox=$2
  input_email_path="tests/data/$email_file"
  echo -n "Testing $email_file... "

  # Run the client, piping the email to it.
  < "$input_email_path" ./client/hermes_client "$SOCKET_PATH" "$MAILDIR_PATH"
  
  expected_path="$MAILDIR_PATH/$expected_mailbox"
  
  # Find the most recently delivered email file in the 'new' subdirectory
  newest_file_name=$(ls -t "$expected_path/new" 2>/dev/null | head -n 1)
  
  if [ -z "$newest_file_name" ]; then
    echo "FAIL: No email file found in $expected_path/new."
    exit 1
  fi
  delivered_file="$expected_path/new/$newest_file_name"

  # Check if the content of the delivered email is identical to the input.
  if diff -q "$input_email_path" "$delivered_file"; then
    echo "OK"    
  else
    echo "FAIL: Content of delivered email does not match input."
    diff "$input_email_path" "$delivered_file"
    exit 1
  fi
}

# Read test cases from cases.txt and execute them.
while read -r email_file expected_mailbox; do
  run_test "$email_file" "$expected_mailbox"
done < cases.txt

echo "--- All tests passed! ---"
exit 0