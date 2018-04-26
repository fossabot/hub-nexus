#!/bin/sh

ARTIFACT=$1
ITERATIONS=$2

if [ -z "$ARTIFACT" ]; then
    echo "No artifact path specified exiting..."
    exit 1
fi

if [ -z "$ITERATIONS" ]; then
    ITERATIONS=5
fi

for i in `seq 1 $ITERATIONS`;
do
    curl -v -F "r=Test" -F "g=com.blackducksoftware.test$i" -F "a=test" -F "v=$i.0.0" -F "p=zip" -F "file=@$ARTIFACT" -u admin:admin123 http://localhost:8081/nexus/service/local/artifact/maven/content
done
