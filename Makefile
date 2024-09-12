
SOURCES = src/main/java
CLASSPATH = target
BINDIR = bin

PACKAGE_DIR = com/ass1
JAVA_TARGET = 21

JAVA_SOURCES = $(shell find $(SOURCES) -name '*.java')
CLASS_FILES = $(patsubst $(SOURCES)/%.java,$(CLASSPATH)/%.class,$(JAVA_SOURCES))

CLIENT_JAR = $(BINDIR)/client.jar
SERVER_JAR = $(BINDIR)/server.jar
PROXY_JAR = $(BINDIR)/proxy.jar

$(CLASSPATH):
	@mkdir -p $(CLASSPATH)

$(BINDIR):
	@mkdir -p $(CLASSPATH)


$(CLASSPATH)/%.class: $(SOURCES)/%.java | $(CLASSPATH)
	javac --target $(JAVA_TARGET) -d $(CLASSPATH) -cp $(CLASSPATH) --source-path $(SOURCES) $<

$(CLIENT_JAR): classfiles | $(BINDIR)
	jar cfe $(CLIENT_JAR) com.ass1.client.Client -C $(CLASSPATH) .

$(SERVER_JAR): classfiles | $(BINDIR)
	jar cfe $(SERVER_JAR) com.ass1.server.Server -C $(CLASSPATH) .

$(PROXY_JAR): classfiles | $(BINDIR)
	jar cfe $(PROXY_JAR) com.ass1.loadbalancer.ProxyServer -C $(CLASSPATH) .

objsize: classfiles | $(BINDIR)
	jar cfe geoname.jar com.ass1.data.Geoname -C $(CLASSPATH) .
	jar cfe objsize.jar com.ass1.data.ObjectSize -C $(CLASSPATH) .

jarfiles: $(CLIENT_JAR) $(SERVER_JAR) $(PROXY_JAR)

classfiles: $(CLASS_FILES)

clean:
	rm -rf $(CLASSPATH)

purge: clean
	rm -rf $(BINDIR)

all: jarfiles


.PHONY: all purge clean test
.DEFAULT_GOAL := classfiles

