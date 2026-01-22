#!/bin/sh
exec java \
  -Dspring.redis.host="${REDIS_HOST:-localhost}" \
  -Dspring.redis.port="${REDIS_PORT:-6379}" \
  -jar app.jar
