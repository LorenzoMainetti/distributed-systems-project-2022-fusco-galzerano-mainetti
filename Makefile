run:
	docker compose up --build | tee log

check:
	cat log | agrep 'node-2;MAIN'