🛒 Fullstack Java Microservices E-commerce Platform
A fully containerized, production-ready Java-based e-commerce application leveraging a modern microservices architecture with Docker Compose orchestration.

🧰 Tech Stack
Frontend: Angular (port 4200)

Backend Services:

eureka-server – Service registry (Spring Boot)

auth-service, cart-service, customer-service, order-service, api-gateway – (Spring MVC)

product-service, order-service – Play Framework

Persistence: PostgreSQL, Spring Data JPA, Hibernate / Jakarta Persistence

Messaging: NATS (primary), REST Template, WebSocket Client

Monitoring: Prometheus & Grafana

CI/CD: GitHub Actions

Orchestration: Docker Compose

🚀 Quick Start
Clone the repository and start the application:

docker compose up


🧑‍💼 Admin Access
An admin user is created automatically at startup:

Email:    doe@luv2code.com  
Password: admin123
🔐 Roles & Features
Admin
Manage categories and products (add, delete, image upload)

View, delete, suspend, or reactivate customers

View and delete orders

Customer
Register and log in

Browse products

Add/remove items from cart

Place orders

📊 Monitoring
Prometheus auto-discovers backend services and scrapes metrics

Grafana dashboard available at http://localhost:3000

🌐 Access Points
Service	Port	URL
Frontend	4200	http://localhost:4200
API Gateway	8080	http://localhost:8080
Eureka Server	8761	http://localhost:8761
Grafana	3000	http://localhost:3000
