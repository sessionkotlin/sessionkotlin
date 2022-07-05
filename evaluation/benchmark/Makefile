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
SJ_TAG = 0.5.0
SJ_CLONE = sj
SJ_TARGET = scribble-dist
TwoBuyerRoles = Seller A B
AdderRoles = Client Server

# Sessionkotlin args
BACKENDS = channels sockets
PROTOCOLS = TwoBuyer Adder

# Benchmarks
N_RUNS = 5
BENCH_CMD = /usr/bin/time --format "%e" --output $(O) --append
SLEEP = 1.0  # seconds

.PHONY: clean
clean:
	@$(G) -q clean
	@rm $(SJ)/src/main/java/* -rf
	@rm $(AUX_FILES) -f
	@rm $(SJ_CLONE) $(SJ_TARGET) -rf

.PHONY: scribble
scribble: $(SJ_TARGET)
	@for r in $(AdderRoles); do \
		./$(SJ_TARGET)/scribblec.sh $(SJ)/Adder.scr -api Adder $$r -d $(SJ)/src/main/java; \
	done
	@for r in $(TwoBuyerRoles); do \
		./$(SJ_TARGET)/scribblec.sh $(SJ)/TwoBuyer.scr -api TwoBuyer $$r -d $(SJ)/src/main/java; \
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
	@for p in $(PROTOCOLS); do \
		for b in $(BACKENDS); do \
      		$(call echof,"#sk/$$p/callbacks/$$b"); \
      		$(call bench_cmd,sessionkotlin-callbacks:run --args "$$b $$p"); \
      		$(call echof,"#sk/$$p/fluent/$$b"); \
      		$(call bench_cmd,sessionkotlin-fluent:run --args "$$b $$p"); \
      	done; \
    	for k in {1..1}; do \
    		$(call echof,"#scribble-java/$$p"); \
    		$(call bench_cmd,scribble-java:run --args "$$p"); \
      	done; \
	done

.PHONY: report
report:
	$(P) report.py

$(SJ_TARGET):
	git clone --depth 1 --branch $(SJ_TAG) -c advice.detachedHead=false https://github.com/scribble/scribble-java.git $(SJ_CLONE)
	@cd $(SJ_CLONE) && mvn install --quiet
	@unzip $(SJ_CLONE)/$(SJ_TARGET)/target/scribble-dist-$(SJ_TAG)-SNAPSHOT.zip -d $(SJ_TARGET)
	@chmod +x $(SJ_TARGET)/scribblec.sh
	@rm $(SJ_CLONE) -rf
