# Signalling Server

WebSocket-based signalling server for WebRTC peer-to-peer communication.

## Overview

This server coordinates WebRTC connections between clients by:
- Authenticating users via JWT tokens
- Managing online user presence
- Routing signalling messages (call initialization, WebRTC offers/answers/candidates)

## Architecture

```
Client A  <-->  Signalling Server  <-->  Client B
              (WebSocket + REST API)
```

### Components

- **AuthProvider**: Abstracts token validation (mock implementation for development)
- **OnlineUsersService**: Tracks connected users and their WebSocket sessions
- **SignallingService**: Business logic for message routing
- **SignallingWebSocketHandler**: WebSocket message handler
- **AuthHandshakeInterceptor**: Validates JWT during WebSocket handshake

## Getting Started

### Prerequisites

- Java 21
- Gradle 8.x

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

Server will start on `http://localhost:8080`

## API Documentation

### WebSocket Endpoint

**URL**: `ws://localhost:8080/signalling`

**Authentication**: Send `Authorization` header with Bearer token during handshake

```javascript
const ws = new WebSocket('ws://localhost:8080/signalling', {
  headers: {
    'Authorization': 'Bearer <your-token>'
  }
});
```

### Message Format

All messages are JSON with the following structure:

```json
{
  "type": "MESSAGE_TYPE",
  "to": 456,
  "payload": { }
}
```

**Note**: `from` field is automatically set by the server from authenticated user ID.

### Message Types

#### 1. CALL_INIT
Initiate a call to another user.

**Client → Server:**
```json
{
  "type": "CALL_INIT",
  "to": 456
}
```

**Server → Recipient:**
```json
{
  "type": "INCOMING_CALL",
  "from": 123,
  "to": 456
}
```

#### 2. CALL_ACCEPT
Accept an incoming call.

**Client → Server:**
```json
{
  "type": "CALL_ACCEPT",
  "to": 123
}
```

**Server → Caller:**
```json
{
  "type": "CALL_ACCEPT",
  "from": 456,
  "to": 123
}
```

#### 3. WEBRTC_OFFER
Send WebRTC offer with SDP.

**Client → Server:**
```json
{
  "type": "WEBRTC_OFFER",
  "to": 456,
  "payload": {
    "sdp": "v=0\r\no=- ..."
  }
}
```

#### 4. WEBRTC_ANSWER
Send WebRTC answer with SDP.

**Client → Server:**
```json
{
  "type": "WEBRTC_ANSWER",
  "to": 123,
  "payload": {
    "sdp": "v=0\r\no=- ..."
  }
}
```

#### 5. WEBRTC_CANDIDATE
Send ICE candidate.

**Client → Server:**
```json
{
  "type": "WEBRTC_CANDIDATE",
  "to": 456,
  "payload": {
    "candidate": "candidate:...",
    "sdpMid": "0",
    "sdpMLineIndex": 0
  }
}
```

#### 6. ERROR
Error message from server.

**Server → Client:**
```json
{
  "type": "ERROR",
  "payload": {
    "message": "User 456 is offline"
  }
}
```

### REST API Endpoints

#### Health Check
```http
GET /api/health
```

**Response:**
```json
{
  "status": "UP",
  "service": "signalling-server",
  "onlineUsers": 5
}
```

#### Online Users
```http
GET /api/users/online
```

**Response:**
```json
{
  "count": 3,
  "users": [123, 456, 789]
}
```

## Configuration

Configuration is managed in `src/main/resources/application.properties`:

```properties
# Server port
server.port=8080

# Auth provider type: mock or rest-api
auth.provider.type=mock

# REST API URL for token validation (when auth.provider.type=rest-api)
auth.rest-api.url=https://itmo-api/validate

# Logging level
logging.level.ru.itmo.calls=DEBUG
```

## Authentication

### Mock Provider (Development)

The mock provider accepts any token and extracts user ID from it:
- If token is a number (e.g., "123"), it uses that as user ID
- Otherwise, generates user ID from token hash

**Example:**
```
Authorization: Bearer 123
```
Results in `userId = 123`

### REST API Provider (Production)

To enable REST API authentication:
1. Set `auth.provider.type=rest-api` in `application.properties`
2. Configure `auth.rest-api.url` to point to your validation endpoint
3. Implement the validation logic in `RestApiAuthProvider`

## Call Flow Example

```
User A (userId=123)                    Signalling Server                    User B (userId=456)
       |                                       |                                       |
       | 1. Connect with token                |                                       |
       |-------------------------------------->|                                       |
       |                                       | 2. Connect with token                |
       |                                       |<--------------------------------------|
       |                                       |                                       |
       | 3. CALL_INIT {to: 456}               |                                       |
       |-------------------------------------->|                                       |
       |                                       | 4. INCOMING_CALL {from: 123}         |
       |                                       |-------------------------------------->|
       |                                       |                                       |
       |                                       | 5. CALL_ACCEPT {to: 123}             |
       |                                       |<--------------------------------------|
       | 6. CALL_ACCEPT {from: 456}           |                                       |
       |<--------------------------------------|                                       |
       |                                       |                                       |
       | 7. WEBRTC_OFFER {to: 456, payload}   |                                       |
       |-------------------------------------->|                                       |
       |                                       | 8. WEBRTC_OFFER {from: 123, payload} |
       |                                       |-------------------------------------->|
       |                                       |                                       |
       |                                       | 9. WEBRTC_ANSWER {to: 123, payload}  |
       |                                       |<--------------------------------------|
       | 10. WEBRTC_ANSWER {from: 456, payload}|                                      |
       |<--------------------------------------|                                       |
       |                                       |                                       |
       | 11-N. WEBRTC_CANDIDATE exchange       |                                       |
       |<------------------------------------->|<------------------------------------->|
       |                                       |                                       |
```

## Logging

Logs are written to:
- Console output
- `logs/signalling-server.log` - All application logs
- `logs/websocket-events.log` - WebSocket-specific events

Log rotation is configured for 30 days retention.

## Error Handling

The server handles the following error scenarios:

1. **Missing/Invalid Token**: Connection rejected with HTTP 401
2. **User Offline**: Error message sent to sender
3. **Invalid Message Format**: Error message sent to sender
4. **Transport Errors**: Connection closed, user unregistered

## Development

### Project Structure

```
src/main/java/ru/itmo/calls/
├── config/              # Configuration classes
│   ├── JacksonConfig.java
│   └── WebSocketConfig.java
├── controller/          # REST controllers
│   └── HealthController.java
├── exception/           # Custom exceptions
│   ├── SignallingException.java
│   ├── UnauthorizedException.java
│   ├── UserOfflineException.java
│   └── InvalidMessageException.java
├── handler/             # WebSocket handlers
│   └── SignallingWebSocketHandler.java
├── model/               # Data models
│   ├── SignalMessage.java
│   ├── SignalType.java
│   └── UserIdentity.java
├── security/            # Authentication
│   ├── AuthProvider.java
│   ├── AuthHandshakeInterceptor.java
│   ├── MockAuthProvider.java
│   └── RestApiAuthProvider.java
└── service/             # Business logic
    ├── OnlineUsersService.java
    └── SignallingService.java
```

## Testing

Connect to the WebSocket endpoint using a tool like `wscat`:

```bash
# Install wscat
npm install -g wscat

# Connect with token
wscat -c ws://localhost:8080/signalling -H "Authorization: Bearer 123"

# Send a message
{"type":"CALL_INIT","to":456}
```

## License

MIT License - see LICENSE file for details
