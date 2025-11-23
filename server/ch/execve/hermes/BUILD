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
        "@maven//:jakarta_inject_jakarta_inject_api",
        "@maven//:com_google_inject_guice",
        "@maven//:org_eclipse_angus_angus_mail",
    ],
)
