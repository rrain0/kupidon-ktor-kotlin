

prod.pull:
	docker compose -f docker-compose-prod.yml --env-file ktor.prod.env pull

prod.up:
	docker compose -f docker-compose-prod.yml --env-file ktor.prod.env up -d --force-recreate

prod.build-up:
	docker compose -f docker-compose-prod.yml --env-file ktor.prod.env up -d --force-recreate --build

prod.down:
	docker compose -f docker-compose-prod.yml --env-file ktor.prod.env down




local.pull:
	docker compose -f docker-compose.yml --env-file ktor.local.env pull

local.up:
	docker compose -f docker-compose.yml --env-file ktor.local.env up -d --force-recreate

local.build-up:
	docker compose -f docker-compose.yml --env-file ktor.local.env up -d --force-recreate --build

local.down:
	docker compose -f docker-compose.yml --env-file ktor.local.env down
