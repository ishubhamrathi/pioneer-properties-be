# API Documentation

Base URL for local development:

```text
http://localhost:8080
```

All request and response bodies are JSON unless noted otherwise.

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

## Cars

Car endpoints are backed by MongoDB.

### POST /cars

Creates a car.

Request:

```json
{
  "brandName": "Toyota",
  "model": "Innova",
  "number": "DL01AB1234"
}
```

Response `201 Created`:

```json
"663f1f6f8c2b1a001234abcd"
```

### GET /cars/{id}

Returns a car by MongoDB object id.

Response `200 OK`:

```json
{
  "brandName": "Toyota",
  "model": "Innova",
  "number": "DL01AB1234"
}
```

Response `404 Not Found` when the car does not exist.

### PUT /cars/{id}

Replaces a car by MongoDB object id.

Request:

```json
{
  "brandName": "Toyota",
  "model": "Fortuner",
  "number": "DL01AB1234"
}
```

Response `200 OK` when updated.

Response `404 Not Found` when the car does not exist.

### DELETE /cars/{id}

Deletes a car by MongoDB object id.

Response `200 OK` when deleted.

Response `404 Not Found` when the car does not exist.

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
