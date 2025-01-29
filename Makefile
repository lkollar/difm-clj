.PHONY: run
run:
	@clj -M -m difm.main

.PHONY: test
test:
	@clj -M:test $(filter-out $@,$(MAKECMDGOALS))