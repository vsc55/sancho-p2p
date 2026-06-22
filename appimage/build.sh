#!/bin/bash
set -e

APPDIR="Sancho.AppDir"

rm -f Sancho-x86_64.AppImage

appimagetool "$APPDIR"