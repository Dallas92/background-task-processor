# Background Task Processor

This is a Spring Boot application which accepts task creation requests which process tasks asynchronously

## Requirements

- Java 17 or higher

## Technologies
- Java 17
- Spring Boot 3.x.x
- Maven
- Postgres
- H2
- Liquibase
- Lombok

## Running the Application

To run the application do the following:

1. Build docker image with command
```bash
docker build -t background-task-processor:latest .
```

2. Run docker-compose with command
```bash
docker-compose up -d 
```

3. Send some request

