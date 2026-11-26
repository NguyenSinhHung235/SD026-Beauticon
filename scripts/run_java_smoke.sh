#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
TMPDIR_TEST=$(mktemp -d)
trap 'rm -rf "$TMPDIR_TEST"' EXIT
javac --release 17 -encoding UTF-8 -d "$TMPDIR_TEST" \
  src/main/java/com/example/vongtay/orders/OrderWorkflow.java \
  src/main/java/com/example/vongtay/sales/SaleSupport.java \
  src/main/java/com/example/vongtay/people/PeopleValidation.java \
  scripts/JavaSmoke.java
java -cp "$TMPDIR_TEST" JavaSmoke
