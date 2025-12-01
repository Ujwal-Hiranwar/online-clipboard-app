# Online Clipboard

This repository contains the source code for a full-stack online clipboard application. The backend is built with Spring Boot, and the frontend is a Next.js application.

---

## Backend (Spring Boot)

# Spring Boot Online Clipboard Application

## Overview
This is a Spring Boot-based backend application for an Online Clipboard system. The application provides a RESTful API to manage clipboard data, allowing users to store, retrieve, and share text snippets efficiently. It is built using Spring Boot and integrates with Jenkins for CI/CD and Docker for containerization, ensuring easy deployment and scalability.

**Repository URL**: [https://github.com/Ujwal-Hiranwar/springboot-backend](https://github.com/Ujwal-Hiranwar/springboot-backend)

## Features
- **End to End Encryption**: Provides endpoints for creating, reading, updating, and deleting clipboard entries.
- **Jenkins CI/CD**: Includes a `Jenkinsfile` for automated building, testing, and deployment.
- **Docker Support**: Includes a `Dockerfile` for containerizing the application.
- **Secure and Scalable**: Built with Spring Boot's robust features for security and scalability.

## Prerequisites
To run this project, ensure you have the following installed on your system:
- **Java**: JDK 17 or later
- **Maven**: For building and managing dependencies
- **Docker**: For containerizing and running the application
- **Git**: To clone the repository

### Installation Instructions
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Ujwal-Hiranwar/springboot-backend.git
   cd springboot-backend
   ```

## Project Setup
1. **Configure Database**:
   - The application uses an H2 in-memory database by default for development. To use a different database (e.g., MySQL or PostgreSQL), update the `application.properties` or `application.yml` file in `src/main/resources` with your database configuration. Example for MySQL:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/clipboard_db
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     spring.jpa.hibernate.ddl-auto=update
     ```

2. **Install Dependencies**:
   - Run the following command to download all required dependencies:
     ```bash
     mvn clean install
     ```

## Running the Application
### Option 1: Run Locally with Maven
1. Start the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
2. The application will start on `http://localhost:8080` (or the port configured in `application.properties`).
3. Access the API endpoints using tools like Postman or cURL. Example:
   ```bash
   curl http://localhost:8080/api/clipboard
   ```

### Option 2: Run with Docker
1. **Run the Docker Container**:
   - Run the container, mapping the application port (default 8080):
     ```bash
     docker run -p 8080:8080 ujwalhiranwar/clipboardimage:fb79101
     ```
   - If using a database, ensure it is accessible to the container and configure environment variables if needed:
     ```bash
     docker run -p 8080:8080 \
       -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/clipboard_db \
       -e SPRING_DATASOURCE_USERNAME=your_username \
       -e SPRING_DATASOURCE_PASSWORD=your_password \
       springboot-clipboard-backend
     ```

3. Run the pipeline in your local jenkins server using jenkins file to build, test, and deploy the application.

## Project Structure
```plaintext
springboot-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/clipboard/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       └── model/
│   │   └── resources/
│   │       └── application.properties
├── Jenkinsfile
├── Dockerfile
├── pom.xml
└── README.md
```

## Troubleshooting
- **Database Connection Issues**: Ensure the database is running and the credentials in `application.properties` are correct.
- **Port Conflicts**: If port 8080 is in use, change the port in `application.properties`:
  ```properties
  server.port=8081
  ```
- **Docker Issues**: It may be possible that port is alredy in use run with differnt port
  ```bash
  docker run -p 8081:8080 ujwalhiranwar/clipboardimage:fb79101
  ```
- **Maven Dependency Issues**: Run `mvn clean install` to refresh dependencies.
- For additional help, refer to the [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/).

## Contributing
Contributions are welcome! To contribute:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Make your changes and commit (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Create a Pull Request.

## License


## Contact
For questions or feedback, contact the repository owner at https://ujwalhiranwar-portfolio.vercel.app/

---

## Frontend (Next.js)

# Next.js Project

## Overview
This is a Next.js project built with modern JavaScript and React. This README provides instructions to set up and run the project locally.

## Live
The project is live at 
   ```bash
   https://foryouclipboardapp.vercel.app/
   ```
## Prerequisites
Before you begin, ensure you have the following installed:
- **Node.js**: Version 18.x or higher
- **npm**: Version 9.x or higher (comes with Node.js) or **yarn** (optional)

## Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Ujwal-Hiranwar/clipboardapp
   cd clipboardapp
   ```

2. **Install dependencies**:
   Using npm:
   ```bash
   npm install
   ```
   Or using yarn:
   ```bash
   yarn install
   ```

## Running the Project
1. **Development Mode**:
   Start the development server with hot reloading:
   ```bash
   npm run dev
   ```
   Or with yarn:
   ```bash
   yarn dev
   ```
   The app will be available at `http://localhost:3000`.

2. **Production Build**:
   To create an optimized production build:
   ```bash
   npm run build
   ```
   Or with yarn:
   ```bash
   yarn build
   ```

## Project Structure
- `pages/`: Contains the Next.js pages (routes).
- `public/`: Static assets like images.
- `components/`: Reusable components.
- `styles/`: CSS or other styling files.
- `package.json`: Project dependencies and scripts.

## Troubleshooting
- Ensure the correct Node.js version is installed.
- If you encounter dependency issues, try deleting `node_modules` and `package-lock.json` (or `yarn.lock`), then reinstall dependencies.
- Check the Next.js documentation for specific error messages: [Next.js Docs](https://nextjs.org/docs).

## Additional Notes
- This project uses Next.js. Ensure familiarity with Next.js routing conventions.
- For further customization, refer to the `next.config.js` file.