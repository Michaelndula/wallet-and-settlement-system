# Wallet & Settlement Microservice
## Project Overview
This project is a complete microservice built with Java (Spring Boot) and React that provides a robust system for managing customer wallet balances and performing daily transaction reconciliations. The system is designed to handle financial transactions such as topping up credits and consuming services from third-party providers, ensuring data integrity and consistency through transactional operations and a message queue.

The application is fully containerized using Docker, allowing for a simple, one-command setup for the entire environment, including the backend, frontend, database, and message queue.

### Core Features
Wallet Management: Allows customers to hold balances (credits).

1. **Transaction Processing:** APIs to top up and consume wallet balances. All operations are transactional and idempotent to prevent duplicate processing.

2. **Ledger System:** All financial movements are recorded in a transaction ledger for auditing and history.

3. **Message Queuing:** Integrates with RabbitMQ to queue all transactions, ensuring reliability and decoupling system components.

4. **Daily Reconciliation:** A module to compare internal transactions against a daily report from an external provider (in CSV format) to identify and report discrepancies.

5. **CSV Export:** Ability to export the detailed reconciliation report as a downloadable CSV file.

### Architecture
This project follows a modern microservice architecture, emphasizing a clean separation of concerns.

- **Backend:** A stateless RESTful API built with Java 17 and Spring Boot 3. It follows a clean architecture pattern (Controller -> Service -> Repository).

- **Controllers:** Expose REST endpoints.

- **Services:** Contain the core business logic.

- **Repositories:** Handle data persistence using Spring Data JPA.

- **Frontend:** A responsive single-page application (SPA) built with React. It communicates with the backend via REST API calls and is served by a lightweight Nginx web server.

- **Database:** MySQL 8.0 is used for data persistence, storing wallet and transaction information.

- **Message Queue:** RabbitMQ is used to queue all top-up and consumption transactions for asynchronous processing or auditing.

- **Containerization:** The entire application (backend, frontend, database, message queue) is containerized with Docker and orchestrated with Docker Compose for easy setup and deployment.

### Prerequisites
Before you begin, ensure you have the following installed on your machine:

1. Docker: [Docker Desktop](https://www.docker.com/products/docker-desktop/).

2. Docker Compose: (Typically included with Docker Desktop)

No local installation of Java, Maven, Node.js, or MySQL is required.

### Setup & Running the Application
This project is designed for a simple, one-command startup.

Clone the repository:

- git clone https://github.com/Michaelndula/wallet-and-settlement-system.git
- cd wallet-system

### Build and run the application:
From the root directory of the project, run the following command:

```docker-compose up --build```

#### This command will:

1. Build the Java backend Docker image (compiling the code and packaging it).

2. Build the React frontend Docker image.

3. Start all the necessary services (backend, frontend, MySQL, RabbitMQ).

### Accessing the Services:
Once the containers are running, you can access the different parts of the system:

**Frontend Application:** http://localhost:80

**Backend API:** http://localhost:8080

**RabbitMQ Management UI:** http://localhost:15672 (user: guest, pass: guest)

### Stopping the Application:
To stop all running containers, press Ctrl + C in the terminal, and then run:

```docker-compose down -v```

## API Endpoints

The following are the available API endpoints for the backend service.

### Wallet Management

| Method | Endpoint | Description | Sample Body 
--- | --- | --- | --- |
POST | ```/api/v1/wallets/{walletId}/topup``` | Increases the balance of a wallet. Creates a new wallet if one doesn't exist. | ```{"amount": 100.00, "transactionId": "TXN-TOPUP-123"}```
POST | ```/api/v1/wallets/{walletId}/consume``` | Deducts balance from a wallet. Fails if funds are insufficient. | ```{"amount": 25.50, "transactionId": "TXN-CONSUME-456"}```
GET | ```/api/v1/wallets/{walletId}/balance``` | Retrieves the current balance of a specific wallet. | ```N/A```


### Reconciliation
| Method | Endpoint | Description | Sample Query 
--- | --- | --- | --- |
GET | ```/api/v1/reconciliation/report``` | Returns a detailed JSON summary of matched and mismatched transactions for a given date. | ```?date=2025-09-12```
GET | ```/api/v1/reconciliation/report/csv``` | Generates and downloads a full reconciliation report in CSV format for a given date. | ```?date=2025-09-12```


## Assumptions Made
- **Wallet Auto-Creation:** For simplicity, a new wallet is automatically created with a zero balance the first time a topup operation is performed for a non-existent walletId. Consumption from a non-existent wallet will fail.

- **Transaction ID Uniqueness:** The transactionId provided in topup and consume requests is assumed to be unique across the entire system. The API is idempotent and will reject any transaction with a previously processed transactionId.

- **Reconciliation File:** The system expects the external report to be a CSV file located in src/main/resources and named using the format external_transactions_YYYY-MM-DD.csv.

- **Stateless Service:** The backend is designed to be stateless. All necessary state is persisted in the MySQL database.

- **Security:** This implementation does not include authentication or authorization. All endpoints are currently public.

## Further Improvements
- **Enhanced Security:** Implement JWT-based authentication and role-based authorization to secure the API endpoints.

- **Unit & Integration Tests:** Expand the test suite to cover more edge cases and business logic scenarios.

- **CI/CD Pipeline:** Set up a continuous integration and deployment pipeline to automate testing and deployments.

- **Flexible Report Handling:** Allow the reconciliation module to process different file formats (e.g., JSON) or fetch reports from an external API or S3 bucket.