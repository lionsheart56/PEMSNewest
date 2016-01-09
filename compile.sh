#!/bin/bash

echo 'compiling...'
javac -cp ./lib/commons-math3-3.2.jar:./lib/platform-2.0.0.jar -d ./bin ./src/ems/*/*.java
echo 'done'
