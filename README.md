# 2025-09-03-javazone-bootiful-spring-ai-with-legend-james-ward
Hi, Spring fans! In this installment I'm joined by the legendary James Ward, AWS developer adovcate extraordinaire and friend to all


## intro
- hard to find the right demo 
- petclinic, peanut, prancer 
- start.spring.io 
- java 25!
- java.. scripts!

## dogs
- no program would be complete without a hello world. BeanRegistrar used to register applicationRunner that prints hello world.
- `beanregistrar`  - show `ApplicationRunner` 
- api version api for retreiving dogs v1 and v2
- virtual threads
  - recovering reactives. it's very good.
- graalvm 
- resource server
- "who let the dogs out?"

## auth
- passkeys 
- ott 
- oauth 
- auth server 

## gateway
- gateway 
- oauth client
- ui 

## dogs
- filter the dogs on `Principal.name`

## assistant
- Spring AI 1.0 Blogs
- bedrock !!
- assistant
  - make sure controller response is a Map
- rag
- prompt 
- tools 
- mcp client for scheduler 
- mcp client for google calendar

## scheduler
- scheduler



# Setup

1. AWS Console & API Keys
1. Database
1. UI


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

