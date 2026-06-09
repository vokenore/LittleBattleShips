#!/bin/bash
mkdir -p out
javac -d out -cp "lib/Java-WebSocket-1.6.0.jar:lib/slf4j-api-2.0.9.jar:lib/slf4j-simple-2.0.9.jar" src/com.littlebattleships/*.java
java -cp "out:lib/Java-WebSocket-1.6.0.jar:lib/slf4j-api-2.0.9.jar:lib/slf4j-simple-2.0.9.jar" com.littlebattleships.Main
run.bat: