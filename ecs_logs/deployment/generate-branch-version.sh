#!/bin/bash

mkdir -p "$BUILD_ARTIFACTSTAGINGDIRECTORY/versions"

VERSION="${BUILD_BUILDNUMBER}"

echo "Versión generada: $VERSION"
echo "##vso[task.setvariable variable=branchVersion]$VERSION"

cat <<EOF > "$BUILD_ARTIFACTSTAGINGDIRECTORY/versions/.version"
{
  "version": "${VERSION}",
  "prefix": "branch",
  "tag": "${VERSION}",
  "buildId": "$BUILD_BUILDID",
  "buildNumber": "$BUILD_BUILDNUMBER",
  "definitionId": "$SYSTEM_DEFINITIONID",
  "definitionName": "$SYSTEM_DEFINITIONNAME",
  "repositoryName": "$BUILD_REPOSITORY_NAME",
  "repositoryId": "$BUILD_REPOSITORY_ID",
  "sourceVersion": "$BUILD_SOURCEVERSION",
  "sourceBranchName": "$BUILD_SOURCEBRANCHNAME",
  "path": "$BUILD_ARTIFACTSTAGINGDIRECTORY/versions/.version"
}
EOF
