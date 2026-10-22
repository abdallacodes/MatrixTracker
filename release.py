#!/usr/bin/env python3
"""
Automated Release Script for MatrixTracker
Handles Git status check, commit, tag detection, patch increment, and remote push.
"""

import re
import subprocess
import sys


def run_command(cmd, allow_failure=False):
    """Executes a command and returns stdout. Exits on failure unless allow_failure is True."""
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0 and not allow_failure:
        print(f"❌ Error running command: {' '.join(cmd)}")
        if result.stderr.strip():
            print(f"   Stderr: {result.stderr.strip()}")
        if result.stdout.strip():
            print(f"   Stdout: {result.stdout.strip()}")
        sys.exit(result.returncode)
    return result.stdout.strip()


def get_current_branch():
    """Gets the active git branch name."""
    branch = run_command(["git", "rev-parse", "--abbrev-ref", "HEAD"])
    return branch if branch and branch != "HEAD" else "main"


def check_and_commit(commit_message):
    """Checks git status and commits uncommitted changes if any exist."""
    print("🔍 Checking repository status...")
    status = run_command(["git", "status", "--porcelain"])
    if status:
        print("✏️  Uncommitted changes detected. Staging and committing...")
        run_command(["git", "add", "-A"])
        run_command(["git", "commit", "-m", commit_message])
        print(f"✅ Committed changes: '{commit_message}'")
    else:
        print("✨ Working tree is clean. No uncommitted changes.")


def fetch_existing_tags():
    """Fetches existing tags from remote and returns list of tag names."""
    print("🌐 Fetching remote tags...")
    run_command(["git", "fetch", "--tags"], allow_failure=True)
    tags_output = run_command(["git", "tag", "-l"])
    return tags_output.splitlines() if tags_output else []


def get_next_version_tag(existing_tags):
    """Parses existing vX.Y.Z tags and computes the next patch tag."""
    pattern = re.compile(r"^v(\d+)\.(\d+)\.(\d+)$")
    parsed_versions = []

    for tag in existing_tags:
        match = pattern.match(tag.strip())
        if match:
            major, minor, patch = map(int, match.groups())
            parsed_versions.append((major, minor, patch))

    if not parsed_versions:
        next_tag = "v1.0.0"
    else:
        parsed_versions.sort()
        highest_major, highest_minor, highest_patch = parsed_versions[-1]
        next_patch = highest_patch + 1
        next_tag = f"v{highest_major}.{highest_minor}.{next_patch}"

    while next_tag in existing_tags:
        match = pattern.match(next_tag)
        if match:
            maj, min_v, pat = map(int, match.groups())
            next_tag = f"v{maj}.{min_v}.{pat + 1}"

    return next_tag


def publish_release(branch, new_tag):
    """Pushes commits and tag to remote repository."""
    print(f"🚀 Pushing branch '{branch}' to origin...")
    run_command(["git", "push", "origin", branch])

    print(f"🏷️  Creating local tag: '{new_tag}'...")
    run_command(["git", "tag", new_tag])

    print(f"📡 Pushing tag '{new_tag}' to origin...")
    run_command(["git", "push", "origin", new_tag])


def main():
    commit_message = sys.argv[1] if len(sys.argv) > 1 else "chore: release build"

    print("========================================")
    print("🚀 Starting Automated MatrixTracker Release")
    print("========================================")

    branch = get_current_branch()
    print(f"📌 Active Branch: {branch}")

    check_and_commit(commit_message)

    existing_tags = fetch_existing_tags()
    next_tag = get_next_version_tag(existing_tags)
    print(f"🎯 Calculated Next Tag: {next_tag}")

    publish_release(branch, next_tag)

    print("========================================")
    print(f"🎉 Success! Released {next_tag} on branch '{branch}'.")
    print("   GitHub Actions will now build and attach app-release.apk.")
    print("========================================")


if __name__ == "__main__":
    main()
