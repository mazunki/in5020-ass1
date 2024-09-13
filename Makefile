SOURCES = src/main/java
TEST_SOURCES = src/test/java
CLASSPATH = target
BINDIR = bin

JAVA_TARGET = 21

JAVA_SOURCES = $(shell find $(SOURCES) -name '*.java')
TEST_JAVA_SOURCES = $(shell find $(TEST_SOURCES) -name '*.java')
CLASS_FILES = $(patsubst $(SOURCES)/%.java,$(CLASSPATH)/%.class,$(JAVA_SOURCES))
TEST_CLASS_FILES = $(patsubst $(TEST_SOURCES)/%.java,$(CLASSPATH)/%.class,$(TEST_JAVA_SOURCES))

CLIENT_JAR = $(BINDIR)/client.jar
SERVER_JAR = $(BINDIR)/server.jar
PROXY_JAR = $(BINDIR)/proxy.jar

$(CLASSPATH):
	@mkdir -p $(CLASSPATH)

$(BINDIR):
	@mkdir -p $(CLASSPATH)

$(CLASSPATH)/%.class: $(SOURCES)/%.java | $(CLASSPATH)
	javac --target $(JAVA_TARGET) -d $(CLASSPATH) -cp $(CLASSPATH) --source-path $(SOURCES) $<

$(CLASSPATH)/%.class: $(TEST_SOURCES)/%.java | $(CLASSPATH)
	javac --target $(JAVA_TARGET) -d $(CLASSPATH) -cp $(CLASSPATH) --source-path $(TEST_SOURCES):$(SOURCES) $<

$(CLIENT_JAR): classfiles | $(BINDIR)
	jar cfe $(CLIENT_JAR) com.ass1.client.Client -C $(CLASSPATH) .

$(SERVER_JAR): classfiles | $(BINDIR)
	jar cfe $(SERVER_JAR) com.ass1.server.Server -C $(CLASSPATH) .

$(PROXY_JAR): classfiles | $(BINDIR)
	jar cfe $(PROXY_JAR) com.ass1.loadbalancer.ProxyServer -C $(CLASSPATH) .

jarfiles: $(CLIENT_JAR) $(SERVER_JAR) $(PROXY_JAR)

classfiles: $(CLASS_FILES)

testfiles: $(TEST_CLASS_FILES)

clean:
	rm -rf $(CLASSPATH)

purge: clean
	rm -rf $(BINDIR)

all: jarfiles

# New target for running tests
test: testfiles
	@echo "Running tests..."
	@java -cp $(CLASSPATH) com.ass1.data.TestDataLoader

.PHONY: all purge clean test
.DEFAULT_GOAL := classfiles
