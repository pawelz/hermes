load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "hermes_server",
    srcs = glob(["ch/execve/hermes/*.java"]),
    main_class = "ch.execve.hermes.HermesServer",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:jakarta_mail_jakarta_mail_api",
        "@maven//:org_eclipse_angus_angus_mail",
        "@maven//:com_google_guava_guava",
    ],
)