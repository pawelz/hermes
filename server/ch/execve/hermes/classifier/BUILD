load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "classifier",
    srcs = glob(["*.java"]),
    visibility = ["//server/ch/execve/hermes:__pkg__"],
    deps = [
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:jakarta_mail_jakarta_mail_api",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
