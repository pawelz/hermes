load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "classifier",
    srcs = ["Classifier.java"],
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:jakarta_mail_jakarta_mail_api",
    ],
)

java_library(
    name = "classifier_impl",
    srcs = glob(["*.java"], exclude=["Classifier.java"]),
    visibility = ["//server/ch/execve/hermes:__pkg__"],
    deps = [
        ":classifier",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:jakarta_mail_jakarta_mail_api",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
