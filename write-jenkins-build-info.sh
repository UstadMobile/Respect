#!/bin/bash

BUILDPROPSFILE=./respect-lib-shared/src/commonMain/composeResources/files/buildinfo.properties

echo "buildtag=$BUILD_TAG" > $BUILDPROPSFILE
echo "buildtime=$(date)" >> $BUILDPROPSFILE
