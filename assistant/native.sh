#!/usr/bin/env bash

./mvnw -DskipTests spring-javaformat:apply

./mvnw -DskipTests -Pnative native:compile  && target/assistant