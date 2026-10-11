#!/usr/bin/env python3
"""Create GitHub labels, milestones and issues from docs/product/ENDOLUTEAL_ROADMAP.md.

Usage:
  scripts/create-issues.py --dry-run   # parse and validate only
  scripts/create-issues.py             # create on the current gh default repo

Idempotent: created issue numbers are stored in .scratch/issues-map.json and
skipped on later runs. A second pass rewrites [ID] references into #numbers.
"""
import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ROADMAP = ROOT / "docs/product/ENDOLUTEAL_ROADMAP.md"
MAP_FILE = ROOT / ".scratch/issues-map.json"
ROADMAP_URL = "docs/product/ENDOLUTEAL_ROADMAP.md"

HEADER = re.compile(r"^### ([A-Z]\d+b?) · (.+)$")
META = re.compile(r"^> milestone: (.+?) \| labels: (.+)$")
REF = re.compile(r"\[([A-Z]\d+b?)\]")

LABEL_COLORS = {
    "type": "1d76db",
    "area": "5319e7",
    "size": "fbca04",
    "agent-ready": "0e8a16",
    "later": "c5def5",
}


def parse():
    issues, current = [], None
    for line in ROADMAP.read_text(encoding="utf-8").splitlines():
        if m := HEADER.match(line):
            current = {"id": m[1], "title": m[2], "body": []}
            issues.append(current)
        elif current is None:
            continue
        elif line.startswith("## "):
            current = None
        elif not current.get("milestone") and (m := META.match(line)):
            current["milestone"] = m[1]
            current["labels"] = [label.strip() for label in m[2].split(",")]
        else:
            current["body"].append(line)
    for issue in issues:
        issue["body"] = "\n".join(issue["body"]).strip()
    return issues


def validate(issues):
    ids = [i["id"] for i in issues]
    errors = [f"duplicate id {i}" for i in set(ids) if ids.count(i) > 1]
    for issue in issues:
        if "milestone" not in issue:
            errors.append(f"{issue['id']}: missing meta line")
        for ref in REF.findall(issue["body"]):
            if ref not in ids:
                errors.append(f"{issue['id']}: unknown reference [{ref}]")
    return errors


def gh(*args):
    return subprocess.run(["gh", *args], check=True, capture_output=True, text=True).stdout.strip()


def main():
    issues = parse()
    errors = validate(issues)
    if errors:
        sys.exit("Roadmap invalid:\n  " + "\n  ".join(errors))
    milestones = list(dict.fromkeys(i["milestone"] for i in issues))
    labels = sorted({label for i in issues for label in i["labels"]})
    print(f"{len(issues)} issues, {len(milestones)} milestones, {len(labels)} labels")
    if "--dry-run" in sys.argv:
        for issue in issues:
            print(f"  {issue['id']:4} [{issue['milestone']}] {issue['title']}")
        return

    repo = gh("repo", "view", "--json", "nameWithOwner", "-q", ".nameWithOwner")
    for label in labels:
        gh("label", "create", label, "--force", "--color", LABEL_COLORS[label.split(":")[0]], "-R", repo)
    existing = set(gh("api", f"repos/{repo}/milestones?state=all&per_page=100", "-q", ".[].title").splitlines())
    for title in milestones:
        if title not in existing:
            gh("api", f"repos/{repo}/milestones", "-f", f"title={title}")

    MAP_FILE.parent.mkdir(exist_ok=True)
    numbers = json.loads(MAP_FILE.read_text()) if MAP_FILE.exists() else {}
    for issue in issues:
        if issue["id"] in numbers:
            continue
        url = gh(
            "issue", "create", "-R", repo,
            "--title", issue["title"],
            "--body", issue["body"],
            "--milestone", issue["milestone"],
            *[arg for label in issue["labels"] for arg in ("--label", label)],
        )
        numbers[issue["id"]] = int(url.rsplit("/", 1)[1])
        MAP_FILE.write_text(json.dumps(numbers, indent=2))
        print(f"  {issue['id']} -> #{numbers[issue['id']]}")

    for issue in issues:
        body = REF.sub(lambda m: f"#{numbers[m[1]]}", issue["body"])
        body = f"Roadmap : `{issue['id']}` dans [{ROADMAP_URL}](../blob/main/{ROADMAP_URL}).\n\n{body}"
        gh("issue", "edit", str(numbers[issue["id"]]), "-R", repo, "--body", body)
    print("References rewritten.")


if __name__ == "__main__":
    main()
