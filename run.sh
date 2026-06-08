#!/bin/bash
mkdir -p out
javac -d out src/com/easybattleships/*.java
java -cp out com.easybattleships.Main