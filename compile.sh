#!/bin/bash

# Remove old class files.
rm *.class

# Generate new class files.
javac OrderStatistics.java

# Make jar file.
jar cvfm OrderStatistics.jar manifest.txt *.class

# Test if new version generates the same output than the old one.
java -jar ./OrderStatistics.jar *.rr | sort -k2,2g -k1,1 > new.r
diff -u new.r old.r
