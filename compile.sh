#!/bin/bash

# Remove old class files.
rm *.class

# Generate new class files.
javac OrderStatistics.java

# Make jar file.
jar cvfm OrderStatistics.jar manifest.txt *.class

# Test if new version generates the same output than the old one.
java -jar ./OrderStatistics.jar test_files/rankratios/*.rr | LC_ALL=C sort -k2,2g -k1,1 > test_files/rankings/new.r
diff -u test_files/rankings/new.r test_files/rankings/old.r
