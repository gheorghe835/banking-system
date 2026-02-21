#!/bin/bash

echo " Deploying Banking System..."

# Oprește containerele vechi
docker-compose down

# Reconstruiește imaginea
docker build -t banking-system:latest .

# Pornește serviciile
docker-compose up -d

# Verifică starea
docker-compose ps

echo "✅ Deployment completed!"