# Tests with control

Better setup for tests with control. For each classifier it should be possible
to provide both positive and negative example messages and the test should make
sure that positive examples are matched while negative examples are not
matched.

# Off-line mode

It should be possible to run hermes on a Mailbox and let it classify all inbox
from the Mailbox.

This could also support a dry-run mode in which Hermes just prints a table of
"From", "Subject", "Classification result".

# Make tests less sensitive to typos in the config

If there is a typo in the class path in the config, the test will hang until
timeout. Instead the test should fail meaningfully.

# User docs

Write up README.md explaining what this is and how to use it.

It would also be good to provide an example of a "private" repository
containing private configs and classifiers.
