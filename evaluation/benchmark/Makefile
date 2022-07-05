SHELL := /bin/bash

# Gradle
GRADLE_FLAGS = --quiet
G = ./gradlew $(GRADLE_FLAGS)

# Scribble
SJ = bench
SJ_TAG = 0.5.0
SJ_CLONE = sj
SJ_TARGET = scribble-dist
TwoBuyerRoles = Seller A B
AdderRoles = Client Server

.PHONY: clean
clean:
	@$(G) -q clean
	@rm $(SJ)/src/main/java/* -rf
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

.PHONY: bench
bench: build
	@for p in $(PROTOCOLS); do \
		for b in $(BACKENDS); do \
		  	$(G) sessionkotlin-callbacks:run --args "$$b $$p"; \
		  	$(G) clean; \
		  	$(G) sessionkotlin-fluent:run --args "$$b $$p"; \
			$(G) clean; \
      	done; \
#    	for k in {1..1}; do \
#    	  	$(G) scribble-java:run --args "$$p"; \
#      	done; \
	done

$(SJ_TARGET):
	git clone --depth 1 --branch $(SJ_TAG) -c advice.detachedHead=false https://github.com/scribble/scribble-java.git $(SJ_CLONE)
	@cd $(SJ_CLONE) && mvn install --quiet
	@unzip $(SJ_CLONE)/$(SJ_TARGET)/target/scribble-dist-$(SJ_TAG)-SNAPSHOT.zip -d $(SJ_TARGET)
	@chmod +x $(SJ_TARGET)/scribblec.sh
	@rm $(SJ_CLONE) -rf
