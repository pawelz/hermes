load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "hermes",
    srcs = glob(["ch/execve/hermes/*.java"]),
    main_class = "ch.execve.hermes.HermesServer",
    visibility = ["//visibility:public"],
)