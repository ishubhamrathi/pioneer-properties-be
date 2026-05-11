# API Documentation

Base URL for local development:

```text
http://localhost:8080
```

All request and response bodies are JSON unless noted otherwise.

Protected endpoints use an `Authorization` header:

```text
Authorization: Bearer <token>
```

## Database Migrations

Postgres migrations run on application startup through Flyway when `db.postgres.url` or `SUPABASE_DB_URL` is configured.

Migration files live in:

```text
server/src/main/resources/db/migration
```

Current migration:

```text
V1__create_locations_table.sql
```

It creates the PostGIS extension if needed, the `locations` table, and indexes for map bounds/search queries.

## General

### GET /

Returns a basic server health response.

Response `200 OK`:

```text
Hello, World!
```

### GET /json/kotlinx-serialization

Returns a sample JSON response.

Response `200 OK`:

```json
{
  "hello": "world"
}
```

## Authentication And Users

Authentication endpoints are backed by MongoDB. Passwords are stored as PBKDF2 hashes. Sign in and sign up responses include a bearer token and the authenticated user, including role.

Supported roles:

```text
USER
ADMIN
```

### POST /api/auth/signup

Creates a user and returns an auth token.

Request:

```json
{
  "email": "admin@example.com",
  "password": "password123",
  "name": "Admin User",
  "role": "ADMIN"
}
```

`role` is optional and defaults to `USER`.

Response `201 Created`:

```json
{
  "token": "eyJ1c2VySWQiOi...",
  "user": {
    "id": "663f1f6f8c2b1a001234abcd",
    "email": "admin@example.com",
    "name": "Admin User",
    "role": "ADMIN"
  }
}
```

Response `400 Bad Request` when the email is invalid, name is blank, password is shorter than 8 characters, or the email is already registered.

### POST /api/auth/signin

Authenticates a user and returns an auth token.

Request:

```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

Response `200 OK`:

```json
{
  "token": "eyJ1c2VySWQiOi...",
  "user": {
    "id": "663f1f6f8c2b1a001234abcd",
    "email": "admin@example.com",
    "name": "Admin User",
    "role": "ADMIN"
  }
}
```

Response `400 Bad Request` when the email or password is invalid.

### GET /api/auth/me

Returns the authenticated user for the bearer token.

Headers:

```text
Authorization: Bearer <token>
```

Response `200 OK`:

```json
{
  "id": "663f1f6f8c2b1a001234abcd",
  "email": "admin@example.com",
  "name": "Admin User",
  "role": "ADMIN"
}
```

Response `401 Unauthorized` when the token is missing, invalid, or expired.

### GET /api/users

Returns all users. Requires `ADMIN`.

Headers:

```text
Authorization: Bearer <admin-token>
```

Response `200 OK`:

```json
[
  {
    "id": "663f1f6f8c2b1a001234abcd",
    "email": "admin@example.com",
    "name": "Admin User",
    "role": "ADMIN"
  }
]
```

Response `403 Forbidden` when the token is missing, invalid, expired, or not an admin token.

### PUT /api/users/{id}/role

Updates a user's role. Requires `ADMIN`.

Headers:

```text
Authorization: Bearer <admin-token>
```

Request:

```json
{
  "role": "USER"
}
```

Response `200 OK`:

```json
{
  "id": "663f1f6f8c2b1a001234abcd",
  "email": "admin@example.com",
  "name": "Admin User",
  "role": "USER"
}
```

Response `403 Forbidden` when the token is missing, invalid, expired, or not an admin token.

## Locations

Location endpoints are backed by the configured Postgres/PostGIS `locations` table. The table name is read from `db.postgres.locationsTable` and defaults to `locations`.

Location shape:

```json
{
  "id": "location-1",
  "name": "Midtown Office",
  "category": "office",
  "lat": 28.6139,
  "lng": 77.209,
  "description": "Optional description",
  "image": "https://example.com/location.jpg",
  "address": "Connaught Place, New Delhi",
  "rating": 4.5,
  "icon": "building",
  "color": "#2563eb"
}
```

Required fields are `id`, `name`, `category`, `lat`, and `lng` in responses.

### GET /api/locations

Returns locations inside map bounds.

Query parameters:

| Name | Required | Description |
| --- | --- | --- |
| `bounds` | Yes | Bounding box as `minLat,minLng,maxLat,maxLng` |
| `zoom` | No | Current map zoom level |

Example:

```text
GET /api/locations?bounds=28.40,76.80,28.90,77.40&zoom=12
```

Response `200 OK`:

```json
{
  "locations": [
    {
      "id": "location-1",
      "name": "Midtown Office",
      "category": "office",
      "lat": 28.6139,
      "lng": 77.209,
      "description": "Optional description",
      "image": null,
      "address": "Connaught Place, New Delhi",
      "rating": 4.5,
      "icon": "building",
      "color": "#2563eb"
    }
  ],
  "hasMore": false
}
```

Response `400 Bad Request` when `bounds` is missing or invalid.

### GET /api/locations/search

Searches locations by name, address, or category. If latitude, longitude, and radius are provided, results are also filtered by distance.

Query parameters:

| Name | Required | Description |
| --- | --- | --- |
| `q` | Yes | Search text |
| `lat` | No | Center latitude for radius search |
| `lng` | No | Center longitude for radius search |
| `radius` | No | Radius in kilometers |

Example:

```text
GET /api/locations/search?q=office&lat=28.6139&lng=77.2090&radius=5
```

Response `200 OK`:

```json
{
  "locations": [],
  "hasMore": false
}
```

Response `400 Bad Request` when `q` is missing.

### GET /api/locations/{id}

Returns a location by id.

Response `200 OK`:

```json
{
  "id": "location-1",
  "name": "Midtown Office",
  "category": "office",
  "lat": 28.6139,
  "lng": 77.209,
  "description": "Optional description",
  "image": null,
  "address": "Connaught Place, New Delhi",
  "rating": 4.5,
  "icon": "building",
  "color": "#2563eb"
}
```

Response `404 Not Found` when the location does not exist.

### POST /api/locations

Creates a location.

Request:

```json
{
  "id": "location-1",
  "name": "Midtown Office",
  "category": "office",
  "lat": 28.6139,
  "lng": 77.209,
  "description": "Optional description",
  "image": "https://example.com/location.jpg",
  "address": "Connaught Place, New Delhi",
  "rating": 4.5,
  "icon": "building",
  "color": "#2563eb"
}
```

`id` is optional. If omitted or blank, the server generates a UUID.

Response `201 Created`:

```json
{
  "id": "location-1",
  "name": "Midtown Office",
  "category": "office",
  "lat": 28.6139,
  "lng": 77.209,
  "description": "Optional description",
  "image": "https://example.com/location.jpg",
  "address": "Connaught Place, New Delhi",
  "rating": 4.5,
  "icon": "building",
  "color": "#2563eb"
}
```

Response `400 Bad Request` when `name` or `category` is blank, `lat` is outside `-90..90`, or `lng` is outside `-180..180`.

### PUT /api/locations/{id}

Updates a location by id.

Request:

```json
{
  "name": "Updated Office",
  "category": "office",
  "lat": 28.614,
  "lng": 77.21,
  "description": "Updated description",
  "image": "https://example.com/location.jpg",
  "address": "Connaught Place, New Delhi",
  "rating": 4.8,
  "icon": "building",
  "color": "#16a34a"
}
```

Response `200 OK`:

```json
{
  "id": "location-1",
  "name": "Updated Office",
  "category": "office",
  "lat": 28.614,
  "lng": 77.21,
  "description": "Updated description",
  "image": "https://example.com/location.jpg",
  "address": "Connaught Place, New Delhi",
  "rating": 4.8,
  "icon": "building",
  "color": "#16a34a"
}
```

Response `400 Bad Request` when `name` or `category` is blank, `lat` is outside `-90..90`, or `lng` is outside `-180..180`.

Response `404 Not Found` when the location does not exist.

### DELETE /api/locations/{id}

Deletes a location by id.

Response `204 No Content` when deleted.

Response `404 Not Found` when the location does not exist.

## Error Responses

Validation errors return plain text with status `400 Bad Request`.

Example:

```text
lat must be between -90 and 90
```

Unhandled errors return plain text with status `500 Internal Server Error`.
