#!/usr/bin/env bash

./mvnw -DskipTests spring-javaformat:apply

git commit -am save

git push origin native