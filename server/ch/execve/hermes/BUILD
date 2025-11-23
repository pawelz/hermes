load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "hermes_server",
    srcs = glob(["*.java"]),
    main_class = "ch.execve.hermes.HermesServer",
    visibility = ["//visibility:public"],
    deps = [
        "//server/ch/execve/hermes/classifier",
        "//server/ch/execve/hermes/classifier:classifier_impl",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:ch_qos_logback_logback_core",
        "@maven//:com_beust_jcommander",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:jakarta_inject_jakarta_inject_api",
        "@maven//:jakarta_mail_jakarta_mail_api",
        "@maven//:org_eclipse_angus_angus_mail",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
