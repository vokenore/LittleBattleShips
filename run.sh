#!/bin/bash
mkdir -p out
javac -d out src/com/battleship/*.java
java -cp out com.battleship.Main