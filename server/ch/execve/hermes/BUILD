load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "hermes_server",
    srcs = glob(["*.java"]),
    main_class = "ch.execve.hermes.HermesServer",
    visibility = ["//visibility:public"],
    deps = [
        "//server/ch/execve/hermes/classifier",
        "@maven//:com_beust_jcommander",
        "@maven//:com_google_guava_guava",
        "@maven//:jakarta_mail_jakarta_mail_api",
        "@maven//:org_eclipse_angus_angus_mail",
    ],
)
