# Bitespeed Identity Reconciliation - Backend

## 🔗 Live API Endpoint

POST:
https://bitspeedidentity.onrender.com/identify

---

## 📦 Tech Stack

- Java 17
- Spring Boot
- PostgreSQL (Cloud - Neon)
- Docker
- Render Deployment

---

## 📥 API Usage

### Endpoint
POST /identify

### Request Body (JSON)

```json
{
  "email": "string (optional)",
  "phoneNumber": "string (optional)"
}
