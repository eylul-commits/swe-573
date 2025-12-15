# SWE-573 — Software Development Practice  
**Course Repository**  
**Author:** Eylül Erdinç  
**Student ID:** 2024719138  

## 📘 Project Title
**The Hive: A Community-Oriented Service Offering Platform**

## 🤝 Project Overview
**The Hive** is a web-based, open-source platform designed to support the exchange of community-based services.  
It enables people to share their time and skills through a **TimeBank system**, where one hour of service equals one credit.  
Instead of money, the platform fosters **reciprocity, trust, and local collaboration**.

---

## Technology Stack

### Backend Technologies

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 17 | Runtime environment |
| **Spring Boot** | 3.2.0 | Application framework |
| **Spring Web** | - | RESTful API development |
| **Spring Data JPA** | - | Database persistence & ORM |
| **Spring Security** | - | Authentication & authorization |
| **Spring WebFlux** | - | Reactive programming for async operations |
| **PostgreSQL** | 16 | Relational database |
| **Maven** | 3.9 | Dependency management & build tool |
| **JWT (jjwt)** | 0.12.3 | Token-based authentication |
| **Stream Chat Java SDK** | 1.37.2 | Real-time messaging functionality |
| **Lombok** | - | Reduces boilerplate code |

### Frontend Technologies

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Vue.js** | ^3.4.21 | Progressive JavaScript framework |
| **TypeScript** | ^5.4.2 | Type-safe JavaScript |
| **Vite** | ^5.1.6 | Build tool & dev server |
| **Pinia** | ^2.1.7 | State management |
| **Tailwind CSS** | ^3.4.1 | Utility-first CSS framework |
| **Element Plus** | ^2.9.2 | Vue 3 UI component library |
| **Radix Vue** | ^1.5.0 | Headless UI components |
| **Lucide Vue Next** | ^0.336.0 | Icon library |
| **Leaflet** | ^1.9.4 | Interactive maps |
| **ngeohash** | ^0.6.3 | Geohash encoding/decoding |
| **Stream Chat** | ^8.40.2 | Real-time chat client |

---

## Prerequisites

Before deploying The Hive, ensure you have the following installed:

### For Docker Compose Deployment (Recommended)
- **Docker**: Version 20.10+ (mine is 28.5.1) ([Installation Guide](https://docs.docker.com/get-docker/))
- **Docker Compose**: Version 2.0+ (mine is v2.40.3) ([Installation Guide](https://docs.docker.com/compose/install/))

### For Local Development (Without Docker)
- **Java**: JDK 17 ([Download](https://adoptium.net/))
- **Maven**: 3.8+ ([Download](https://maven.apache.org/download.cgi))
- **Node.js**: 20+ ([Download](https://nodejs.org/))
- **PostgreSQL**: 16+ ([Download](https://www.postgresql.org/download/))

---

## Deployment Instructions

### Option 1: Docker Compose (All-in-One)

Docker Compose automatically orchestrates all three services (database, backend, frontend) with proper dependencies.

#### Step 1: Clone the Repository
```bash
git clone https://github.com/eylul-commits/swe-573.git
cd swe-573
```

#### Step 2: Configure Environment Variables

Create a `.env` file in the project root:

```bash
# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production

# Stream Chat Configuration (for real-time messaging)
STREAM_CHAT_SECRET=your-stream-chat-secret
VITE_STREAM_CHAT_API_KEY=your-stream-chat-api-key

# Cloudinary Configuration (for image uploads)
VITE_CLOUDINARY_CLOUD_NAME=your-cloudinary-cloud-name
VITE_CLOUDINARY_UPLOAD_PRESET=your-cloudinary-upload-preset
```

> **Note**: For Stream Chat, sign up at [getstream.io](https://getstream.io/). For Cloudinary, sign up at [cloudinary.com](https://cloudinary.com/).

#### Step 3: Build and Start All Services
```bash
docker-compose up --build
```
#### Step 4: Access the Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Database**: localhost:5432

#### Step 5: Stop the Services
```bash
# Stop and remove containers
docker-compose down

# Stop and remove containers + volumes
docker-compose down -v
```

---

### Option 2: Individual Docker Containers (Manual Control)

I used individual Docker containers for Render deployment since the platform doesn't support Docker Compose. While these commands show the manual process, Render automatically detects and builds from the Dockerfiles, so I have not used these.

#### Step 1: Create Docker Network
```bash
docker network create thehive-network
```

#### Step 2: Start PostgreSQL Database
```bash
docker run -d \
  --name thehive-postgres \
  --network thehive-network \
  -e POSTGRES_DB=thehive \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -v postgres_data:/var/lib/postgresql/data \
  -v $(pwd)/backend/src/main/resources/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql \
  -v $(pwd)/backend/src/main/resources/data.sql:/docker-entrypoint-initdb.d/02-data.sql \
  postgres:16-alpine
```

#### Step 3: Build and Run Backend
```bash
# Build the backend image
cd backend
docker build -t thehive-backend .

# Run the backend container
docker run -d \
  --name thehive-backend \
  --network thehive-network \
  -e DB_HOST=thehive-postgres \
  -e DB_NAME=thehive \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=your-super-secret-jwt-key \
  -e STREAM_CHAT_SECRET=your-stream-chat-secret \
  -p 8080:8080 \
  thehive-backend
```

#### Step 4: Build and Run Frontend
```bash
# Build the frontend image
cd ../frontend
docker build \
  --build-arg VITE_API_BASE_URL=http://localhost:8080/api \
  --build-arg VITE_STREAM_CHAT_API_KEY=your-stream-chat-api-key \
  --build-arg VITE_CLOUDINARY_CLOUD_NAME=your-cloudinary-cloud-name \
  --build-arg VITE_CLOUDINARY_UPLOAD_PRESET=your-cloudinary-upload-preset \
  -t thehive-frontend .

# Run the frontend container
docker run -d \
  --name thehive-frontend \
  --network thehive-network \
  -p 3000:80 \
  thehive-frontend
```

#### Step 5: Verify All Containers Are Running
```bash
docker ps
```

You should see three running containers:
- `thehive-postgres`
- `thehive-backend`
- `thehive-frontend`

#### Step 6: Stop and Clean Up
```bash
# Stop individual containers
docker stop thehive-frontend thehive-backend thehive-postgres

# Remove containers
docker rm thehive-frontend thehive-backend thehive-postgres

# Remove network
docker network rm thehive-network
```

---

## Local Development (Without Docker)

### Backend Setup

#### Step 1: Install PostgreSQL
Download and install PostgreSQL 16 from [postgresql.org](https://www.postgresql.org/download/)

#### Step 2: Create Database
```bash
psql -U postgres
CREATE DATABASE thehive;
\c thehive
\i backend/src/main/resources/schema.sql
\i backend/src/main/resources/data.sql
\q
```

#### Step 3: Configure Environment Variables
```bash
# Linux/Mac
export DB_HOST=localhost
export DB_NAME=thehive
export DB_USER=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=your-secret-key
export STREAM_CHAT_SECRET=your-stream-chat-secret

# Windows PowerShell
$env:DB_HOST="localhost"
$env:DB_NAME="thehive"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
$env:JWT_SECRET="your-secret-key"
$env:STREAM_CHAT_SECRET="your-stream-chat-secret"
```

#### Step 4: Run Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on http://localhost:8080

### Frontend Setup

#### Step 1: Install Dependencies
```bash
cd frontend
npm install
```

#### Step 2: Configure Environment Variables

Create a `.env.local` file in the `frontend` directory:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_STREAM_CHAT_API_KEY=your-stream-chat-api-key
VITE_CLOUDINARY_CLOUD_NAME=your-cloudinary-cloud-name
VITE_CLOUDINARY_UPLOAD_PRESET=your-cloudinary-upload-preset
```

#### Step 3: Run Development Server
```bash
npm run dev
```

The frontend will start on http://localhost:5173 (Vite default port)

---


## Testing

### Backend Tests
```bash
cd backend
mvn test
```

---

## Author

**Eylül Erdinç**  
Student ID: 2024719138  
GitHub: [@eylul-commits](https://github.com/eylul-commits)