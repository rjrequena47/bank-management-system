#!/usr/bin/env bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE customers_db;
    CREATE DATABASE accounts_db;
    GRANT ALL PRIVILEGES ON DATABASE customers_db TO admin;
    GRANT ALL PRIVILEGES ON DATABASE accounts_db TO admin;
EOSQL