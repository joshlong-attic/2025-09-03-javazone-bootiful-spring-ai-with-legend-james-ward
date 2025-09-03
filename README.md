# 2025-09-03-javazone-bootiful-spring-ai-with-legend-james-ward
Hi, Spring fans! In this installment I'm joined by the legendary James Ward, AWS developer adovcate extraordinaire and friend to all


## intro
- hard to find the right demo 
- petclinic, peanut, prancer 
- start.spring.io 
- java 25!
- java.. scripts!

## dogs
- api version api for retreiving dogs v1 and v2
- `beanregistrar` 
- virtual threads
- graalvm 
- resource server

## auth
- passkeys 
- ott 
- oauth 
- auth server 

## gateway
- gateway 
- oauth client
- ui 

## use auth in dogs
- filter in dogs on Principal name

## ai assistant
- bedrock !!
- assistant
- rag
- prompt 
- tools 
- mcp client for scheduler 
- mcp client for google calendar

## mcp scheduler
- scheduler



# Setup

```
cd ui
python3 -m http.server 8020
```

```
docker compose up
```

```
PGPASSWORD=secret psql -Umyuser -dmydatabase -hlocalhost -fdata.sql
```

```
cd dogs
./mvnw spring-boot:run
```

```
cd auth
./mvnw spring-boot:run
```

```
cd gateway
./mvnw spring-boot:run
```

```
cd scheduler
./mvnw spring-boot:run
```

```
export SPRING_AI_BEDROCK_AWS_ACCESS_KEY=<YOURS>
export SPRING_AI_BEDROCK_AWS_SECRET_KEY=<YOURS>
cd assistant
./mvnw spring-boot:run
```

http://127.0.0.1:8081/

