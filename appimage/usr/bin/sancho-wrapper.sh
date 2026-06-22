#!/bin/bash

DIR="$(dirname "$(readlink -f "$0")")"

export SWT_LIBRARY_PATH="$DIR/lib"
export LD_LIBRARY_PATH="$DIR/lib:$LD_LIBRARY_PATH"
export JAVA_TOOL_OPTIONS="--enable-native-access=ALL-UNNAMED"

cd "$DIR"

exec ./sancho
