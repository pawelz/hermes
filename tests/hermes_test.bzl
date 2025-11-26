"""Starlark macro for defining hermes integration tests."""

def hermes_test(name, cases, data = [], config = "//tests/config"):
    """
    A macro that creates a hermes integration test.

    Args:
      name: The name of the test.
      cases: A list of tuples, where each tuple is (email_file, expected_mailbox).
      data: A list of data dependencies, typically the email files.
      config: The label of the configuration filegroup to use.
    """

    # 1. Generate the content for the cases.txt file.
    cases_content = "\n".join(["%s %s" % (case[0], case[1]) for case in cases])

    # 2. Create a target for the cases.txt file.
    cases_file_name = name + "_cases.txt"
    native.genrule(
        name = name + "_cases_generator",
        outs = [cases_file_name],
        cmd = "echo '" + cases_content + "' > $@",
    )

    # 3. Create the sh_test target.
    native.sh_test(
        name = name,
        srcs = ["//tests:test_runner.sh"],
        data = [
            ":" + cases_file_name,
            "//client:hermes_client",
            "//server/ch/execve/hermes:hermes_server",
            config,
        ] + data,
        # Rename cases.txt to be at the root for the script to find it.
        args = ["mv $(location :%s) cases.txt && ./tests/test_runner.sh" % cases_file_name],
    )