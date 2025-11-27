filegroup(
    name = "important_emails",
    srcs = [
        "alpacas_1.msg",
        "alpacas_2.msg",
    ],
    visibility = ["//tests:__pkg__"],
)

filegroup(
    name = "spam_emails",
    srcs = [
        "spam_bad_1.msg",
        "spam_bad_2.msg",
        "spam_bad_3.msg",
        "spam_bad_4.msg",
    ],
    visibility = ["//tests:__pkg__"],
)

filegroup(
    name = "inbox_emails",
    srcs = glob([
        "empty.msg",
        "spam_good_*.msg",
    ]),
    visibility = ["//tests:__pkg__"],
)