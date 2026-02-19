INFISICAL_ENV ?= dev
INFISICAL_PATH ?= /lgtm

.PHONY: up down restart logs ps

up:
	infisical run --env=$(INFISICAL_ENV) --path=$(INFISICAL_PATH) -- docker compose up -d

down:
	docker compose down

restart:
	docker compose down
	infisical run --env=$(INFISICAL_ENV) --path=$(INFISICAL_PATH) -- docker compose up -d

logs:
	docker compose logs -f $(SVC)

ps:
	docker compose ps

env-check:
	infisical run --env=$(INFISICAL_ENV) --path=$(INFISICAL_PATH) -- env | grep -E '^(GARAGE_|GRAFANA_|...)'