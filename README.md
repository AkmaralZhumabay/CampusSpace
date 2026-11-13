# CampusSpace

## 1. Project Description

CampusSpace is a university room booking application designed to help students find available study rooms, view room information, check university schedules and make room reservations.

The application provides a visual campus map, room search with different selection strategies, booking management and notifications about booking events.

## 2. Technologies Used

- Java 17
- Spring Boot 4.1.2
- Spring Data JPA
- Spring Web MVC
- Spring Boot Validation
- H2 Database
- Maven
- HTML, CSS, JavaScript

## 3. Project Structure

```text
campusspace/
├── src/
│   ├── main/
│   │   ├── java/com/campusspace/
│   │   └── resources/static/
│   └── test/
├── sample-data/
├── scripts/
├── pom.xml
├── mvnw
└── mvnw.cmd
```

## 4. Requirements

- Java Development Kit (JDK) 17
- Maven Wrapper (included in the project)
- A modern web browser

The application uses Spring Boot 4.1.2 and an H2 database.

## 5. Running the Application

1. Clone or download the repository.
2. Open the `campusspace` project folder.
3. Make sure Java is installed.
4. Run the application using:

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

5. Open the application in your browser:

```text
http://localhost:8080
```

## 6. Running Tests

Run all automated tests using:

**Windows:**
```bash
mvnw.cmd test
```

**Linux / macOS:**
```bash
./mvnw test
```

## 7. Sample Data

The project includes sample files in the `sample-data/` directory:

- `schedule.csv`
- `schedule-test.csv`

The project also includes a room initialization script in `scripts/seed_rooms.py`.

## 8. Main Features

- Browse available university rooms.
- Search for rooms using Best Fit and Largest First strategies.
- View room details and campus map.
- Create and manage room bookings.
- Import university schedules from CSV files.
- Receive notifications when booking events occur.
- View personal booking records.

## 9. Design Patterns

The application demonstrates six design patterns:

| Category | Pattern |
|---|---|
| Creational | Builder |
| Creational | Factory Method |
| Structural | Adapter |
| Structural | Facade |
| Behavioral | Observer |
| Behavioral | Strategy |

## 10. Repository

GitHub: https://github.com/AkmaralZhumabay/CampusSpace

## 11. Contributors

- Kanat Sayazhan
- Zhumabay Akmaral
- Satybaldy Amina
