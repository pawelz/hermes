load("@rules_cc//cc:defs.bzl", "cc_binary", "cc_library")

cc_binary(
    name = "hermes_client",
    srcs = ["hermes.c"],
    deps = [":libmaildir"],
    visibility = ["//visibility:public"],
)

cc_library(
    name = "libmaildir",
    srcs = ["libmaildir.c"],
    hdrs = ["libmaildir.h"],
    visibility = ["//visibility:private"],
)