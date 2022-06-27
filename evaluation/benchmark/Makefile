# Programs
P = python3
MARKDOWN_HANDLER = xdg-open

# Output
O = bench.txt
REPORT_PY = report.py
REPORT_MD = report.md
AUX_FILES = $(O) *.svg $(REPORT_MD)

# Gradle
GRADLE_FLAGS = --quiet
G = ./gradlew $(GRADLE_FLAGS)

# Scribble
SJ = scribble-java
PROTOCOL = GuessingGame
ROLES = "Magician" "User"
SJ_TAG = 0.5.0
SJ_CLONE = sj
SJ_TARGET = scribble-dist

# Sessionkotlin args
CHANNELS = channels
SOCKETS = sockets
KEYS = $(CHANNELS) $(SOCKETS)

# Benchmarks
N_RUNS = 20
BENCH_CMD = /usr/bin/time --format "%e" --output $(O) --append
SLEEP = 1.0  # seconds

.PHONY: clean
clean:
	@$(G) -q clean
	@rm $(SJ)/src/main/java/* -rf
	@rm $(AUX_FILES) -f
	@rm $(SJ_CLONE) $(SJ_TARGET) -rf

.PHONY: scribble
scribble:
	@for r in $(ROLES); do \
  		./$(SJ_TARGET)/scribblec.sh $(SJ)/$(PROTOCOL).scr -api $(PROTOCOL) $$r -d $(SJ)/src/main/java; \
  	done

.PHONY: protocols
protocols:
	@$(G) protocols:run

.PHONY: build
build: protocols scribble
	@$(G) build

# Aux functions
define repeat_n
	for i in {1..$(N_RUNS)}; do \
  		$(1); \
  		sleep $(SLEEP); \
  	done
endef

define bench_cmd
	$(call repeat_n,$(BENCH_CMD) $(G) $(1))
endef
define echof
	echo $(1) >> $(O)
endef
# end Aux functions

.PHONY: bench
bench: build
	@: > $(O)  # create or truncate file
	@for k in $(KEYS); do \
  		$(call echof,"#sk/callbacks/$$k"); \
  		$(call bench_cmd,sessionkotlin-callbacks:run --args $$k); \
  		$(call echof,"#sk/fluent/$$k"); \
  		$(call bench_cmd,sessionkotlin-fluent:run --args $$k); \
  	done
	@for k in {1..1}; do \
		$(call echof,"#scribble-java"); \
		$(call bench_cmd,scribble-java:run); \
  	done

.PHONY: report
report:
	$(P) report.py

.PHONY: install-scribble
install-scribble:
	git clone --depth 1 --branch $(SJ_TAG) -c advice.detachedHead=false https://github.com/scribble/scribble-java.git $(SJ_CLONE)
	@cd $(SJ_CLONE) && mvn install --quiet
	@unzip $(SJ_CLONE)/$(SJ_TARGET)/target/scribble-dist-$(SJ_TAG)-SNAPSHOT.zip -d $(SJ_TARGET)
	@chmod +x $(SJ_TARGET)/scribblec.sh
	@rm $(SJ_CLONE) -rf
