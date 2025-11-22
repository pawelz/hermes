load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "hermes_server",
    srcs = glob(["ch/execve/hermes/*.java"]),
    main_class = "ch.execve.hermes.HermesServer",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_guava_guava",
    ],
)