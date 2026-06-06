@echo off
mkdir out 2>nul
javac -d out src/com/battleship/*.java
java -cp out com.battleship.Main