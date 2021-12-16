#!/bin/bash

javac -source 1.7 -target 1.7 -d bin -cp lib/jlibrosa-1.1.8-SNAPSHOT-jar-with-dependencies.jar src/com/example/Main.java
java -cp lib/jlibrosa-1.1.8-SNAPSHOT-jar-with-dependencies.jar:bin com.example.Main ./cough_data_613 ./mfcc_features 8000 96