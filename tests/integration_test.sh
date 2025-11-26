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

echo "--- Starting Hermes Integration Test ---"

# Bazel sets TEST_TMPDIR for us to use as a scratch directory.
TMPDIR="$TEST_TMPDIR"

# WTF there is a limit on Unix Domain Socket path length. 110 chars in Linux,
# 108 chars in Darwin. Of course the bazel test paths are longer than that.
# So we are trying to just use a relative file name and hope for the best.
SOCKET_PATH="hermes.sock"
MAILDIR_PATH="$TMPDIR/maildir"
DB_PATH="$TMPDIR/hermes.db"
LOG_PATH="$TMPDIR/hermes.log"

# The config files are provided as a data dependency.
# The path is relative to the workspace root (which is the CWD for the test).
CONFIG_DIR="tests/config"

# Start the Hermes server binary in the background.
# The path is relative to the workspace root.
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

# 3. Define a test function to run the client and check the result.
run_test() {
  email_file=$1
  expected_mailbox=$2
  input_email_path="tests/data/$email_file"
  echo -n "Testing $email_file... "

  # Run the client, piping the email to it.
  # The client binary path is also relative to the workspace root.
  < "$input_email_path" ./client/hermes_client "$SOCKET_PATH" "$MAILDIR_PATH"
  
  # The client now prints the full path, so we check if the maildir exists.
  expected_path="$MAILDIR_PATH/$expected_mailbox"
  
  # Find the most recently delivered email file in the 'new' subdirectory
  newest_file_name=$(ls -t "$expected_path/new" | head -n 1)
  delivered_file="$expected_path/new/$newest_file_name"

  if [ -z "$delivered_file" ]; then
    echo "FAIL: No email file found in $expected_path/new."
    exit 1
  fi

  # Check if the content of the delivered email is identical to the input.
  if diff -q "$input_email_path" "$delivered_file"; then
    echo "OK"    
  else
    echo "FAIL: Content of delivered email does not match input."
    diff "$input_email_path" "$delivered_file"
    exit 1
  fi
}

# Run tests for different emails.
run_test "alpacas_1.msg" "important"
run_test "alpacas_2.msg" "important"
run_test "empty.msg" "INBOX"
run_test "spam_bad_1.msg" "spam"
run_test "spam_bad_2.msg" "spam"
run_test "spam_bad_3.msg" "spam"
run_test "spam_bad_4.msg" "spam"
run_test "spam_good_1.msg" "INBOX"
run_test "spam_good_2.msg" "INBOX"
run_test "spam_good_3.msg" "INBOX"
run_test "spam_good_4.msg" "INBOX"

echo "--- All tests passed! ---"
exit 0