#!/usr/bin/env bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE ms_customers_db;
    CREATE DATABASE ms_accounts_db;
    GRANT ALL PRIVILEGES ON DATABASE ms_customers_db TO admin;
    GRANT ALL PRIVILEGES ON DATABASE ms_accounts_db TO admin;
EOSQL
