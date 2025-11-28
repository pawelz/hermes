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

CASES_FILE=$1
HERMES_SERVER_BIN=$2
HERMES_CLIENT_BIN=$3
CONFIG_BUILD_FILE=$4
EXTRA_CLASS_PATH_JARS=$5

if [ -n "$5" ]; then
  shift 5
else
  shift 4
fi

echo "--- Starting Hermes Test Runner ---"

# Bazel sets TEST_TMPDIR for us to use as a scratch directory.
TMPDIR="$TEST_TMPDIR"

# Use a relative file name for the socket to avoid path length limits.
SOCKET_PATH="hermes.sock"
MAILDIR_PATH="$TMPDIR/maildir"
DB_PATH="$TMPDIR/hermes.db"
LOG_PATH="$TMPDIR/hermes.log"

# Derive the config directory from the path of its BUILD file.
CONFIG_DIR=$(dirname "$CONFIG_BUILD_FILE")

echo "Starting server..."
if [ -n "$EXTRA_CLASS_PATH_JARS" ]; then
  # The fifth argument is a space-separated list of JARs.
  # We replace spaces with colons to create a valid Java classpath.
  EXTRA_CLASS_PATH=$(echo "$EXTRA_CLASS_PATH_JARS" | tr ' ' ':')
  # Start the server with the extra classpath flag.
  $HERMES_SERVER_BIN \
    --socket-path "$SOCKET_PATH" \
    --database-path "$DB_PATH" \
    --log-file "$LOG_PATH" \
    --config-dir "$CONFIG_DIR" \
    --wrapper_script_flag=--main_advice_classpath="$EXTRA_CLASS_PATH" &
else
  # Start the server normally.
  $HERMES_SERVER_BIN \
    --socket-path "$SOCKET_PATH" \
    --database-path "$DB_PATH" \
    --log-file "$LOG_PATH" \
    --config-dir "$CONFIG_DIR" &
fi

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
  # The genrule in the macro now provides a path relative to the data filegroup's package.
  # We assume the data filegroup is in a directory named after the test case.
  input_email_path="$email_file"
  echo -n "Testing $email_file... "

  # Run the client, piping the email to it.
  < "$input_email_path" "$HERMES_CLIENT_BIN" "$SOCKET_PATH" "$MAILDIR_PATH"
  
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
done < "$CASES_FILE"

echo "--- All tests passed! ---"
exit 0