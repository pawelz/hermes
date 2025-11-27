load("//tests/infra:hermes_test.bzl", "hermes_test")

hermes_test(
    name = "important_test",
    result = "important",
    data = "//tests/data:important_emails",
)

hermes_test(
    name = "spam_test",
    result = "spam",
    data = "//tests/data:spam_emails",
)

hermes_test(
    name = "inbox_test",
    result = "INBOX",
    data = "//tests/data:inbox_emails",
)