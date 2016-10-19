DIFF	= diff
JAR	= jar
JAVA	= java
JAVAC	= javac
RM	= rm
SORT	= sort

build:
	# Remove old class files and jar file.
	$(RM) *.class OrderStatistics.jar
	# Generate new class files.
	$(JAVAC) OrderStatistics.java
	# Make jar file.
	$(JAR) cvfm OrderStatistics.jar manifest.txt *.class

test:
	# Test if new version generates the same output than the old one.
	$(JAVA) -jar OrderStatistics.jar test_files/rankratios/*.rr | LC_ALL=C $(SORT) -k2,2g -k1,1 > test_files/rankings/new.r
	@echo "Difference between old and new rankings file:"
	$(DIFF) -u test_files/rankings/new.r test_files/rankings/old.r
