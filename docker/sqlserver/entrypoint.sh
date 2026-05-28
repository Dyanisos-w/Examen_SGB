#!/bin/bash
set -e

# Start SQL Server in background
/opt/mssql/bin/sqlservr &
SQLSERVR_PID=$!

# Detect sqlcmd path (SQL Server 2022 uses mssql-tools18)
if [ -f /opt/mssql-tools18/bin/sqlcmd ]; then
    SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
elif [ -f /opt/mssql-tools/bin/sqlcmd ]; then
    SQLCMD="/opt/mssql-tools/bin/sqlcmd"
else
    echo "ERROR: sqlcmd not found"
    exit 1
fi

echo "Waiting for SQL Server to accept connections..."
for i in $(seq 1 60); do
    if $SQLCMD -S localhost -U SA -P "Padel!123" -Q "SELECT 1" -b -C 2>/dev/null; then
        echo "SQL Server is ready (attempt $i)"
        break
    fi
    echo "Not ready yet... ($i/60)"
    sleep 3
done

# Copier le backup dans le répertoire attendu par le script SQL
mkdir -p /var/opt/mssql/backup
cp /usr/src/app/init/PadelDB.bak /var/opt/mssql/backup/PadelDB.bak 2>/dev/null || true

echo "Running init scripts..."
for f in /usr/src/app/init/*.sql; do
    echo "  -> $f"
    $SQLCMD -S localhost -U SA -P "Padel!123" -i "$f" -C
done

echo "Database initialization complete."

wait $SQLSERVR_PID
