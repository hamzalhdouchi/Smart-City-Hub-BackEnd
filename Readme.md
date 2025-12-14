# Smart City Hub – Monorepo

Plateforme web de gestion intelligente des incidents urbains (Smart City Hub), composée de :
- Un **backend** Spring Boot (Java 17, PostgreSQL, JWT, WebSocket)
- Un **frontend** React (Vite, Axios, Leaflet, WebSocket)

---

## Repositories

- `backend/` : API REST + WebSocket + sécurité
- `frontend/` : Interface utilisateur (citoyen, agent, superviseur, admin)

---

# Backend – `backend/README.md`

Plateforme backend pour la gestion des services urbains : utilisateurs, incidents, notifications temps réel, évaluations et statistiques.

## Stack Technique

- Java 17, Spring Boot 3.x
- Spring Web, Spring Data JPA, Spring Security, Spring Validation
- Spring WebSocket (STOMP)
- PostgreSQL 15+
- JWT (io.jsonwebtoken)
- MapStruct, Lombok
- Springdoc OpenAPI (Swagger)
- Maven

## Structure du Projet

```
backend/
├── src/
│ ├── main/
│ │ ├── java/com/smartcity/hub/
│ │ │ ├── config/ # Sécurité, WebSocket, CORS, Swagger
│ │ │ ├── controller/ # REST Controllers (Auth, Users, Incidents, ...)
│ │ │ ├── dto/ # DTOs d'entrée/sortie
│ │ │ ├── entity/ # Entités JPA (User, Incident, Category, ...)
│ │ │ ├── exception/ # Gestion globale des exceptions
│ │ │ ├── mapper/ # MapStruct mappers (Entity <-> DTO)
│ │ │ ├── repository/ # Repositories JPA
│ │ │ ├── security/ # JWT, filtres, services de sécurité
│ │ │ ├── service/ # Logique métier
│ │ │ └── SmartCityHubApplication.java
│ │ └── resources/
│ │ ├── application.yml # Config (DB, JWT, WebSocket, etc.)
│ │ └── data.sql # Données initiales (catégories, admin, etc.)
│ └── test/ # Tests unitaires / intégration
└── pom.xml
```


## Prérequis

- Java 17
- Maven 3.8+
- PostgreSQL 15+
- Git

## Configuration Base de Données

Créer la base et l’utilisateur :

```
CREATE DATABASE smart_city_hub;
CREATE USER smart_city_user WITH ENCRYPTED PASSWORD 'smart_city_pass';
GRANT ALL PRIVILEGES ON DATABASE smart_city_hub TO smart_city_user;
Configurer `backend/src/main/resources/application.yml` :
spring:
datasource:
url: jdbc:postgresql://localhost:5432/smart_city_hub
username: smart_city_user
password: smart_city_pass
jpa:
hibernate:
ddl-auto: update
show-sql: true
properties:
hibernate:
format_sql: true

jwt:
secret: "CHANGE_ME_WITH_A_STRONG_SECRET_KEY"
access-token-validity: 86400000 # 24h
refresh-token-validity: 2592000000 # 30 jours

server:
port: 8080
```


##  Lancement du Backend

```
cd backend

Build
mvn clean install

Run
mvn spring-boot:run

```



- API : `http://localhost:8080`
- Swagger UI : `http://localhost:8080/swagger-ui/index.html`

## Authentification & Rôles

- Authentification par **JWT** (`Authorization: Bearer <token>`).
- Rôles :
    - `ROLE_USER` : Citoyen
    - `ROLE_AGENT` : Agent municipal
    - `ROLE_SUPERVISOR` : Superviseur
    - `ROLE_ADMIN` : Administrateur

### Endpoints principaux (exemples)

- `POST /auth/register` – Inscription citoyen
- `POST /auth/login` – Login & obtention JWT
- `POST /auth/refresh` – Refresh token
- `GET /api/users/me` – Profil utilisateur connecté

- `GET /api/categories` – Liste catégories incidents
- `POST /api/categories` – Créer catégorie (ADMIN)

- `POST /api/incidents` – Créer incident
- `GET /api/incidents` – Lister incidents (avec filtres)
- `GET /api/incidents/{id}` – Détails incident
- `PATCH /api/incidents/{id}/status` – Changer statut (AGENT/SUPERVISOR)
- `POST /api/incidents/{id}/assign` – Assigner agent (SUPERVISOR/ADMIN)

- `POST /api/incidents/{id}/photos` – Upload photos
- `GET /api/incidents/{id}/photos` – Lister photos

- `POST /api/incidents/{id}/comments` – Ajouter commentaire
- `GET /api/incidents/{id}/comments` – Liste commentaires

- `POST /api/incidents/{id}/rate` – Évaluer intervention
- `GET /api/statistics/global` – Statistiques globales (ADMIN)

## WebSocket (Notifications Temps Réel)

- Endpoint WebSocket : `/ws`
- Protocole : STOMP + SockJS
- Préfixes :
    - Application : `/app`
    - Topics : `/topic`
    - Queues privées : `/user/queue`

Exemples de destinations :
- `/topic/incidents` – Nouveaux incidents / changements globaux
- `/user/queue/notifications` – Notifications privées par utilisateur

## Tests

```
mvn test
```




