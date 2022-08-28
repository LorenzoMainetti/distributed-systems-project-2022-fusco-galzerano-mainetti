run:
	docker compose up --build | tee log

check:
	cat log | agrep 'node-1;MAIN' | grep 0.2
	cat log | agrep 'node-1;MAIN' | grep 0.3
	cat log | agrep 'node-1;MAIN' | grep 0.4
	cat log | agrep 'node-1;MAIN' | grep 0.5
	cat log | agrep 'node-2;MAIN' | grep 0.2
	cat log | agrep 'node-2;MAIN' | grep 0.3
	cat log | agrep 'node-2;MAIN' | grep 0.4
	cat log | agrep 'node-2;MAIN' | grep 0.5
	cat log | agrep 'node-3;MAIN' | grep 0.2
	cat log | agrep 'node-3;MAIN' | grep 0.3
	cat log | agrep 'node-3;MAIN' | grep 0.4
	cat log | agrep 'node-3;MAIN' | grep 0.5