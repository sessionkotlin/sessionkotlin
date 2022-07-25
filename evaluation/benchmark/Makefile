SHELL := /bin/bash

# Gradle
GRADLE_FLAGS = --quiet
G = ./gradlew $(GRADLE_FLAGS)

.PHONY: clean
clean:
	@$(G) -q clean
	@rm $(SJ_CLONE) $(SJ_TARGET) -rf

.PHONY: protocols
protocols:
	@$(G) protocols:run

.PHONY: build
build: protocols #scribble
	@$(G) build

bench: build
	@$(G) jmh
