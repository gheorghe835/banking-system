#!/bin/bash

BACKUP_DIR="./backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="banking_system"
DB_USER="root"
DB_PASS="1234"

mkdir -p $BACKUP_DIR

echo " Creating database backup..."

docker exec banking-mysql mysqldump -u $DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

echo "✅ Backup saved to $BACKUP_DIR/backup_$DATE.sql"