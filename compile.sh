#!/usr/bin/env bash
javac OrderStatistics.java
jar cvfm OrderStatistics.jar manifest.txt *.class
# Test:
java -jar ./OrderStatistics.jar *.rr | sort -gk2 > new.r
java -classpath ~/tools/OrderStats/OrderStats.jar analysis.OrderStats3 *.rr | sort -gk2 > old.r
diff new.r old.r