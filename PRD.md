# Smart City Hub - Product Requirements Document (PRD)

## 1. Overview

**Smart City Hub** is a web-based platform for intelligent urban incident management. The platform allows citizens to report urban issues (potholes, broken streetlights, graffiti, etc.), municipal agents to handle them, supervisors to oversee operations, and administrators to manage the entire system.

## 2. Project Objectives

- Enable citizens to report and track urban incidents
- Provide municipal agents with tools to manage and resolve incidents
- Give supervisors visibility into operations and agent assignments
- Allow administrators to manage users, categories, and view analytics
- Real-time notifications via WebSocket for immediate updates

## 3. Technical Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Database | PostgreSQL 15+ / H2 (dev) |
| Security | Spring Security + JWT |
| API Documentation | Springdoc OpenAPI (Swagger) |
| Real-time | Spring WebSocket (STOMP) |
| ORM | Spring Data JPA |
| Utilities | Lombok, MapStruct |
| Build | Maven |

## 4. User Roles & Permissions

### 4.1 ROLE_USER (Citizen)
- Register and login
- Create new incidents
- View own incidents and their status
- Upload photos to incidents
- Add comments to incidents
- Rate incident resolutions
- Receive notifications about own incidents

### 4.2 ROLE_AGENT (Municipal Agent)
- View assigned incidents
- Update incident status (IN_PROGRESS, RESOLVED, etc.)
- Add comments/notes to incidents
- Upload photos (before/after resolution)

### 4.3 ROLE_SUPERVISOR
- View all incidents in their area
- Assign incidents to agents
- Change incident status
- View agent performance

### 4.4 ROLE_ADMIN (Administrator)
- Full CRUD on users
- Manage incident categories
- View global statistics
- System configuration

## 5. Core Features

### 5.1 Authentication & Authorization
- [x] User registration (citizens)
- [x] JWT-based authentication
- [x] Access token + Refresh token mechanism
- [x] Role-based access control
- [ ] Password reset functionality

### 5.2 User Management
- [ ] User profile (view/update)
- [ ] Admin: CRUD operations on users
- [ ] Admin: User role management
- [ ] User avatar upload

### 5.3 Incident Management
- [ ] Create incident with location (lat/lng), description, category
- [ ] List incidents with filters (status, category, date, location)
- [ ] View incident details
- [ ] Update incident status
- [ ] Assign incident to agent
- [ ] Incident history/audit trail

### 5.4 Incident Categories
- [ ] CRUD categories (Admin only)
- [ ] List active categories (public)

### 5.5 Photos
- [ ] Upload multiple photos per incident
- [ ] List photos for an incident
- [ ] Delete photos

### 5.6 Comments
- [ ] Add comments to incidents
- [ ] List comments for an incident
- [ ] Edit/Delete own comments

### 5.7 Ratings & Evaluations
- [ ] Rate resolved incidents (1-5 stars)
- [ ] Add feedback text
- [ ] View ratings statistics

### 5.8 Notifications (WebSocket)
- [ ] Real-time incident updates
- [ ] Private notifications per user
- [ ] Notification history

### 5.9 Statistics & Analytics
- [ ] Global statistics (Admin)
- [ ] Incidents by status
- [ ] Incidents by category
- [ ] Average resolution time
- [ ] Agent performance metrics

## 6. API Endpoints

### 6.1 Authentication (`/auth`)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/auth/register` | Register new citizen | Public |
| POST | `/auth/login` | Login & get JWT tokens | Public |
| POST | `/auth/refresh` | Refresh access token | Authenticated |
| POST | `/auth/logout` | Logout (invalidate token) | Authenticated |

### 6.2 Users (`/api/users`)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/users/me` | Get current user profile | Authenticated |
| PUT | `/api/users/me` | Update current user profile | Authenticated |
| GET | `/api/users` | List all users | Admin |
| GET | `/api/users/{id}` | Get user by ID | Admin |
| PUT | `/api/users/{id}` | Update user | Admin |
| DELETE | `/api/users/{id}` | Delete user | Admin |
| GET | `/api/users/agents` | List agents | Supervisor, Admin |

### 6.3 Categories (`/api/categories`)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/categories` | List all categories | Public |
| POST | `/api/categories` | Create category | Admin |
| PUT | `/api/categories/{id}` | Update category | Admin |
| DELETE | `/api/categories/{id}` | Delete category | Admin |

### 6.4 Incidents (`/api/incidents`)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/incidents` | Create incident | User |
| GET | `/api/incidents` | List incidents (filtered) | Authenticated |
| GET | `/api/incidents/{id}` | Get incident details | Authenticated |
| PUT | `/api/incidents/{id}` | Update incident | Owner, Agent |
| PATCH | `/api/incidents/{id}/status` | Change status | Agent, Supervisor |
| POST | `/api/incidents/{id}/assign` | Assign to agent | Supervisor, Admin |
| GET | `/api/incidents/my` | Get user's incidents | User |

### 6.5 Photos (`/api/incidents/{id}/photos`)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/incidents/{id}/photos` | Upload photo(s) | Owner, Agent |
| GET | `/api/incidents/{id}/photos` | List photos | Authenticated |
| DELETE | `/api/incidents/{id}/photos/{photoId}` | Delete photo | Owner, Admin |

### 6.6 Comments (`/api/incidents/{id}/comments`)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/incidents/{id}/comments` | Add comment | Authenticated |
| GET | `/api/incidents/{id}/comments` | List comments | Authenticated |
| PUT | `/api/incidents/{id}/comments/{commentId}` | Update comment | Owner |
| DELETE | `/api/incidents/{id}/comments/{commentId}` | Delete comment | Owner, Admin |

### 6.7 Ratings (`/api/incidents/{id}/rate`)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/incidents/{id}/rate` | Rate resolution | Incident Owner |
| GET | `/api/incidents/{id}/rating` | Get rating | Authenticated |

### 6.8 Statistics (`/api/statistics`)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/statistics/global` | Global stats | Admin |
| GET | `/api/statistics/by-category` | Stats by category | Admin, Supervisor |
| GET | `/api/statistics/by-status` | Stats by status | Admin, Supervisor |

## 7. Data Models

### 7.1 User
```java
- id: Long
- email: String (unique)
- password: String (hashed)
- firstName: String
- lastName: String
- phone: String
- role: Role (enum)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
- active: Boolean
```

### 7.2 Incident
```java
- id: Long
- title: String
- description: String
- latitude: Double
- longitude: Double
- address: String
- status: IncidentStatus (enum)
- priority: Priority (enum)
- reporter: User
- assignedAgent: User
- category: Category
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
- resolvedAt: LocalDateTime
```

### 7.3 Category
```java
- id: Long
- name: String
- description: String
- icon: String
- active: Boolean
```

### 7.4 Photo
```java
- id: Long
- incident: Incident
- url: String
- uploadedBy: User
- createdAt: LocalDateTime
```

### 7.5 Comment
```java
- id: Long
- incident: Incident
- author: User
- content: String
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### 7.6 Rating
```java
- id: Long
- incident: Incident
- user: User
- stars: Integer (1-5)
- feedback: String
- createdAt: LocalDateTime
```

### 7.7 Notification
```java
- id: Long
- user: User
- title: String
- message: String
- type: NotificationType (enum)
- read: Boolean
- createdAt: LocalDateTime
```

## 8. Enums

### 8.1 Role
- `ROLE_USER`
- `ROLE_AGENT`
- `ROLE_SUPERVISOR`
- `ROLE_ADMIN`

### 8.2 IncidentStatus
- `REPORTED` - Initial state
- `VALIDATED` - Confirmed by supervisor
- `IN_PROGRESS` - Being handled
- `RESOLVED` - Issue fixed
- `CLOSED` - Verified and closed
- `REJECTED` - Invalid/duplicate report

### 8.3 Priority
- `LOW`
- `MEDIUM`
- `HIGH`
- `URGENT`

### 8.4 NotificationType
- `INCIDENT_CREATED`
- `INCIDENT_ASSIGNED`
- `STATUS_CHANGED`
- `COMMENT_ADDED`
- `INCIDENT_RESOLVED`

## 9. Implementation Phases

### Phase 1: Foundation ✅ (Current)
- [x] Project setup with Spring Boot
- [x] Maven dependencies configuration
- [ ] Application configuration (application.yml)
- [ ] Database schema setup

### Phase 2: Security & Authentication
- [ ] Security configuration
- [ ] JWT implementation (generation, validation)
- [ ] User entity and repository
- [ ] Auth controller (register, login, refresh)
- [ ] Custom UserDetailsService

### Phase 3: Core Entities
- [ ] Category entity, DTO, repository, service, controller
- [ ] Incident entity, DTO, repository, service, controller
- [ ] MapStruct mappers

### Phase 4: Extended Features
- [ ] Photo upload/management
- [ ] Comments system
- [ ] Rating system

### Phase 5: Real-time & Analytics
- [ ] WebSocket configuration
- [ ] Notification service
- [ ] Statistics endpoints

### Phase 6: Testing & Documentation
- [ ] Unit tests
- [ ] Integration tests
- [ ] Swagger documentation

## 10. Non-Functional Requirements

### 10.1 Security
- All passwords must be hashed using BCrypt
- JWT tokens must have secure secrets
- API endpoints must be protected based on roles
- CORS must be properly configured

### 10.2 Performance
- API response time < 200ms for simple queries
- Support for pagination on list endpoints
- Efficient database queries with proper indexing

### 10.3 Scalability
- Stateless authentication (JWT)
- Clean separation of concerns
- Modular architecture

## 11. Getting Started

```bash
# Clone the repository
git clone <repo-url>

# Navigate to project
cd smart-city-hub

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

Access:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

---

*Last Updated: December 2024*
