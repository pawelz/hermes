load("//tests:hermes_test.bzl", "hermes_test")

sh_test(
    name = "test_runner",
    srcs = ["test_runner.sh"],
    visibility = ["//visibility:public"],
)

hermes_test(
    name = "hermes_integration_test",
    cases = [
        ("alpacas_1.msg", "important"),
        ("alpacas_2.msg", "important"),
        ("empty.msg", "INBOX"),
        ("spam_bad_1.msg", "spam"),
        ("spam_bad_2.msg", "spam"),
        ("spam_bad_3.msg", "spam"),
        ("spam_bad_4.msg", "spam"),
        ("spam_good_1.msg", "INBOX"),
        ("spam_good_2.msg", "INBOX"),
        ("spam_good_3.msg", "INBOX"),
        ("spam_good_4.msg", "INBOX"),
    ],
    data = ["//tests/data"],
)