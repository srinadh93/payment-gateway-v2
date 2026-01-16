# 💳 Event-Driven Payment Gateway

An event-driven microservices backend for processing payments asynchronously. Built with **Spring Boot**, **Redis**, **PostgreSQL**, and **Docker**.

## 🚀 Overview

This project simulates a real-world payment processing system. It separates the "receiving" of a payment from the "processing" of a payment to handle high loads efficiently.

1.  **API Service:** Accepts payment requests instantly and pushes them to a Queue.
2.  **Redis Queue:** Acts as a buffer to hold jobs.
3.  **Worker Service:** Picks up jobs from the queue, processes them (simulates bank delays), and updates the database.
4.  **Webhooks:** Automatically notifies external services (like a merchant website) upon completion.
5.  **Dashboard:** A real-time frontend to monitor transaction statuses.

## 📸 Screenshots
![alt text](<Screenshot 2026-01-16 163938-1.png>)

## 🛠️ Tech Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3
* **Database:** PostgreSQL (Persistent Storage)
* **Queue:** Redis (Message Broker)
* **Containerization:** Docker & Docker Compose
* **Frontend:** HTML5 / JavaScript (Fetch API)

## 📂 Architecture

```mermaid
graph LR
  Client[Client/Dashboard] --> API[API Service]
  API --> Redis[Redis Queue]
  Redis --> Worker[Worker Service]
  Worker --> DB[(PostgreSQL)]
  Worker --> Webhook[External Webhook]
⚡ How to Run
Prerequisites
Docker Desktop installed and running.

Steps
Clone the repository:

Bash

git clone [https://github.com/srinadh93/payment-gateway-v2.git](https://github.com/srinadh93/payment-gateway-v2.git)
Navigate to the project folder:

Bash

cd payment-gateway-v2
Start the services:

Bash

docker-compose up --build
Open the Dashboard:

Navigate to the project folder in your file explorer.

Double-click index.html to open it in your browser.

🔌 API Endpoints
1. Create a Payment
POST http://localhost:8080/api/v1/payments

Body:

JSON

{
  "amount": 500.00,
  "webhookUrl": "[https://webhook.site/your-unique-url](https://webhook.site/your-unique-url)"
}
Response:

JSON

{
  "id": "pay_12345...",
  "status": "pending",
  "message": "Processing started"
}
2. Get All Payments
GET http://localhost:8080/api/v1/payments

Response:

JSON

[
  {
    "id": "pay_12345...",
    "amount": 500.0,
    "status": "success",
    "createdAt": "2026-01-16T10:00:00"
  }
]
🧪 Features Tested
✅ Asynchronous Processing: API responds immediately; Worker processes in the background.

✅ Persistence: Data survives container restarts (PostgreSQL).

✅ Fault Tolerance: Random 20% failure simulation logic included.

✅ Webhooks: Sends HTTP POST notifications to external URLs upon success.

✅ CORS: Enabled for frontend dashboard integration.

📝 Project Structure
Bash

payment-gateway-v2/
├── backend/
│   ├── src/main/java/com/gateway/
│   │   ├── api/            # REST Controllers
│   │   ├── entity/         # Database Models
│   │   ├── jobs/           # Job Definitions
│   │   ├── repository/     # JPA Repositories
│   │   ├── workers/        # Background Worker Logic
│   │   └── PaymentGatewayApplication.java
│   ├── Dockerfile          # API Docker Image
│   ├── Dockerfile.worker   # Worker Docker Image
│   └── pom.xml             # Dependencies
├── docker-compose.yml      # Orchestration
├── index.html              # Frontend Dashboard
└── README.md               # Documentation
👤 Author
Srinadh

GitHub Profile


---

```powershell
git add README.md
git commit -m "Fix README formatting"
git push origin main