@echo off
mkdir out 2>nul
javac -d out src/com.easybattleships/*.java
java -cp out com.easybattleships.Main