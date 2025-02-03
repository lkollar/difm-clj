.PHONY: run
run:
	@clj -M -m difm.main

.PHONY: test
test:
	@clj -M:test $(filter-out $@,$(MAKECMDGOALS))

.PHONY: lint
lint:
	@clj -M:format/check

.PHONY: format
format:
	@clj -M:format/fix
