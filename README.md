# Wordz Spring Boot Application

A Spring Boot web application built with Java 21 and Maven.

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher

## Project Structure

```
wordz/
├── src/
│   ├── main/
│   │   ├── java/dev/newpower/
│   │   │   ├── WordzApplication.java
│   │   │   └── controller/
│   │   │       └── HomeController.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/dev/newpower/
│           └── WordzApplicationTests.java
├── pom.xml
└── README.md
```

## Running the Application

### Using Maven

1. **Compile and run:**
   ```bash
   mvn spring-boot:run
   ```

2. **Build and run JAR:**
   ```bash
   mvn clean package
   java -jar target/wordz-0.0.1-SNAPSHOT.jar
   ```

### Using IDE

1. Import the project as a Maven project
2. Run `WordzApplication.java` as a Java application

## Accessing the Application

Once the application is running, you can access:

- **Home page:** http://localhost:8084/
- **Health check:** http://localhost:8084/health
- **API Documentation:** http://localhost:8084/api-docs.html

## Features

- Spring Boot 3.2.0
- Java 21
- Maven build system
- Web starter for REST endpoints
- DevTools for development convenience
- Scrabble puzzle generation
- Tile bag management
- Word validation
- Comprehensive API documentation

## API Features

The application provides a RESTful API for:

- **Puzzle Generation:** Create Scrabble puzzles with valid word placements
- **Tile Management:** Draw, reset, and shuffle Scrabble tile bags
- **Game Information:** Get letter point values and distribution data
- **Health Monitoring:** Application status and health checks

## Development

The application uses the `dev.newpower` base package and includes:

- Main application class with Spring Boot auto-configuration
- REST controller with basic endpoints
- Application properties for configuration
- Basic test setup

## Building

```bash
mvn clean compile
```

## Testing

```bash
mvn test
```

## Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Test
- Spring Boot DevTools (development only) 


## Initialize Heroku App
```bash
heroku login
heroku git:remote -a wordz-app
```

## Deploying to Heroku
```bash
heroku login
git push heroku main
```
