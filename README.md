🚀 Fullstack Java Microservices E-commerce Platform
A fully containerized, production-grade e-commerce platform, built with a modern microservices architecture. Engineered for extensibility, observability, and orchestration — deployable via Docker Compose or Kubernetes with Terraform, across Windows and Ubuntu environments.

Whether you're debugging from WSL or deploying to the cloud, this project scales with your ambition.

🧰 Tech Stack
Layer	Tech
Frontend	Angular (port 4200)
Backend Services	Spring Boot, Spring MVC, Play Framework
Service Registry	Eureka Server (port 8761)
Persistence	PostgreSQL, Spring Data JPA, Hibernate
Messaging	NATS (primary), REST Template, WebSocket Client
Monitoring	Prometheus + Grafana (port 3001)
CI/CD	GitHub Actions
Orchestration	Docker Compose, Kubernetes via Terraform
⚡ Quick Start (Local with Docker Compose)
bash
docker compose up
➡ Starts all microservices, database, Grafana & Prometheus instantly via docker-compose.yml. No clustering, no staging – just raw service access to get your hands dirty.

🧠 Advanced Setup (Infrastructure-as-Code with Terraform + K8s)
If you want production simulation with proper service orchestration:

Navigate to infra/ folder

Run:

bash
terraform init
terraform plan
terraform apply
🧩 Requirements:

Terraform installed

Valid kubectl context (your Kubernetes cluster must be reachable)

✅ Once applied, services will be deployed to your cluster.

🔄 Port Management (Cross-Platform Scripts)
After services are live, use included scripts to manage port forwarding dynamically:

Script	OS	Purpose
forward_ports.ps1	Windows	Auto-open forwards for backend services
forward_ports.sh	Ubuntu	Same logic, Linux-native
cleanup_ports.ps1	Windows	Kill all kubectl port-forward processes
cleanup_ports.sh	Ubuntu	Bash equivalent with port awareness
🎺 Start with forward_ports, cleanup anytime via corresponding cleanup_ports.

🧑‍💼 Admin Access (Dev Mode)
Pre-seeded admin account:

Email: doe@luv2code.com

Password: admin123

🔓 You can change or disable this in auth-service.

🛒 Roles & Features
🔐 Admin
Manage categories & products (add, delete, image upload)

Suspend/reactivate customers

View & delete orders

👤 Customer
Register & log in

Browse & filter products

Add/remove items from cart

Place orders with persistence

📊 Monitoring & Observability
Prometheus: Auto-scrapes services via discovery

Grafana: Fully themed dashboards available at http://localhost:3001

🌐 Access Points
Service	Port	URL
Frontend (Angular)	4200	http://localhost:4200
API Gateway	8080	http://localhost:8080
Eureka Server	8761	http://localhost:8761
Grafana	3000	http://localhost:3001
💬 Questions / Collaboration
Have questions, suggestions, or want to collaborate on future versions?

📬 Contact: philip_rivers85@yahoo.com

I built this system to be modular, scalable, and pragmatic. If you value full control and real-world deployability, you're in the right repo.