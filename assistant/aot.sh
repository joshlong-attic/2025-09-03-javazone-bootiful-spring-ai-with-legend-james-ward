#!/usr/bin/env bash
rm -rf target

./mvnw -DskipTests spring-javaformat:apply

./mvnw -DskipTests package spring-boot:process-aot