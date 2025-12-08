#!/usr/bin/env bash
rm -rf target
./mvnw -DskipTests package spring-boot:process-aot