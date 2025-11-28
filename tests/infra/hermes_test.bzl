"""Starlark macro for defining hermes integration tests."""

def hermes_test(name, result, data, config = "@hermes//tests/config", deps = []):
    """
    A macro that creates a hermes integration test.

    Args:
      name: The name of the test.
      result: The expected mailbox for all emails in the data set.
      data: A filegroup containing the email files for this test.
      config: The label of the configuration filegroup to use.
      deps: A list of extra Java dependencies (e.g., custom classifiers).
    """

    # 1. Generate the cases.txt file using a genrule.
    # This rule iterates through the source files from the 'data' filegroup
    # and pairs each file's basename with the expected 'result'.
    cases_file_name = name + "_cases.txt"
    native.genrule(
        name = name + "_cases_generator",
        srcs = [data],
        outs = [cases_file_name],
        cmd = "for f in $(SRCS); do echo \"$$f %s\"; done > $@" % result,
    )

    test_data = [
        cases_file_name,
        "@hermes//client:hermes_client",
        "@hermes//server/ch/execve/hermes:hermes_server",
        config,
        data,
        "%s:BUILD" % config.rsplit(":", 1)[0],
    ] + deps
    test_args = [
        "$(location %s)" % cases_file_name,
        "$(location @hermes//server/ch/execve/hermes:hermes_server)",
        "$(location @hermes//client:hermes_client)",
        "$(location %s:BUILD)" % config.rsplit(":", 1)[0],
    ]

    if deps:
        # $(locations) expands to a space-separated list of file paths.
        test_args.append("$(locations %s)" % " ".join(deps))

    native.sh_test(
        name = name,
        srcs = ["@hermes//tests/infra:test_runner.sh"],
        data = test_data,
        args = test_args,
    )