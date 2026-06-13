# CircleGuard project

Video URL: _

Members: Heiner Danit Rincón (A0040) and Karold Mejia (A00401806)

**Absolute privacy. High-speed containment. Secure campus.**

CircleGuard is a university contact tracing and fencing system designed to identify interconnected contact groups, referred to as "circles", and apply rapid health fences while preserving individual anonymity.

---

## Vision and mission

The vision is a university campus where health containment speed outpaces laboratory confirmation timelines without compromising student privacy. CircleGuard leverages campus-native intelligence, including class schedules and WiFi infrastructure, to deliver a human-validated graph-based protection ecosystem.

### Key differentiators
- **Privacy as code**: Zero real-name exposure outside a secure health centre vault.
- **Recursive containment**: Status promotion cascades from suspect to probable to confirmed that trigger in milliseconds.
- **Campus integration**: Smart check-ins using existing WiFi access point triangulation and Bluetooth low energy.

---

## Success metrics

| Metric | Target | Measurement |
|:---|:---|:---|
| **Containment speed** | Less than 60 seconds | Automated test of promotion engine cascade |
| **Privacy compliance** | 100 per cent anonymity | Penetration test on graph database with zero real names |
| **Check-in adoption** | Greater than 70 per cent | Analytics on scheduled class contact validation |
| **False positive rate** | Less than 15 per cent | Post-fence surveys of actual versus suspected contact |
| **System uptime** | 99.5 per cent | 7:00 AM to 10:00 PM during academic peak hours |

---

## Architecture overview

CircleGuard follows a microservice architecture built on a hybrid data model.

### Core engine
- **Status promotion machine**: Uses Neo4j for recursive graph traversals to identify contacts within a 14-day temporal window.
- **Anonymisation vault**: A segregated PostgreSQL vault handles salted-hash identity mapping, compliant with FERPA regulations.
- **Event-driven core**: Apache Kafka manages asynchronous status changes, audit logs and notification dispatches.

### Services directory
- **Auth service**: Dual-chain LDAP for university access and local for guest access with dynamic role-based access control.
- **Identity service**: Cryptographic vault for anonymising real identities.
- **Promotion service**: Status engine for recursive graph processing.
- **Notification service**: Multi-channel dispatcher supporting push, email and SMS.
- **Form service**: Dynamic health questionnaire engine.
- **Gateway service**: Campus entry validation using signed time-limited QR tokens.
- **Dashboard service**: Geospatial hotspot analytics with privacy preservation.
- **File service**: Secure certificate and document storage compatible with S3.

---

## Technical stack

| Layer | Technology | Rationale |
|:---|:---|:---|
| **Backend** | Spring Boot 4 with Java 21 | Enterprise-grade maturity and low-latency Jakarta EE support |
| **Graph database** | Neo4j version 5.26 | High-performance recursive traversals unreachable with SQL |
| **Relational database** | PostgreSQL version 16 | ACID compliant storage for identity and configuration |
| **Message bus** | Apache Kafka version 7.6 | Persistent audit-trailed event log for status dispatches |
| **Caching** | Redis version 7.2 | L2 distributed cache for rapid entry-gate status validation |
| **Mobile and web** | Expo with React Native | Unified codebase across iOS, Android and browser |
| **Infrastructure** | Kubernetes | Orchestration for high availability and auto-scaling |

---

## Roadmap

### Phase one: MVP (Intelligence core) current

- Status promotion machine supporting suspect to probable to confirmed transitions
- Temporal graph with 14-day time-to-live edges
- Multi-channel fence notifications for push, email and SMS
- Health centre de-identification console (in progress)

### Phase two: Growth (Spatial intelligence)

- WiFi access point triangulation integration
- Campus entry validation with Gatekeeper QR integration
- Learning management system integration for remote attendance status automation

### Phase three: Vision (Full ecosystem)

- Off-campus circle detection via peer-to-peer Bluetooth
- Global health dashboard with hotspot visualisation
- Laboratory API bridge for automated test result ingestion

---

## Local development

### Infrastructure

Ensure Docker is installed and start the middleware stack using the following command:

```bash
docker-compose -f docker-compose.dev.yml up -d
```

The middleware includes PostgreSQL, Neo4j, Kafka, Zookeeper, Redis and OpenLDAP.

### Build and run

CircleGuard uses Gradle for parallel builds across services.

```bash
# Start all microservices in parallel
./gradlew bootRun --parallel

# Start a specific service
./gradlew :services:<service-name>:bootRun
```

### API exploration

Every service exposes an OpenAPI 3.0 interface. Once running, visit the following URL:

`http://localhost:<service-port>/swagger-ui/index.html`

---

## Frontend development

The frontend is built using Expo with React Native, supporting iOS, Android and web from a single codebase located in the `mobile` directory.

### Prerequisites

Ensure that Node.js is installed and load the dependencies:

```bash
cd mobile
npm install
```

### Run the application

The application can be run in various modes depending on the target platform.

| Platform | Command | Notes |
|:---|:---|:---|
| **Development menu** | `npm run start` | Opens the Expo Go start-up menu |
| **Android** | `npm run android` | Requires Android Studio, an emulator or a connected device |
| **iOS** | `npm run ios` | Requires macOS with Xcode or Simulator installed |
| **Web browser** | `npm run web` | Launches the dashboard or application in the default browser |

### Testing

To run frontend unit and component tests, use the following command:

```bash
npm run test
```

---

## Testing

System integrity is maintained through multi-level testing.

| Command | Scope |
|:---|:---|
| `./gradlew test` | Full system suite covering unit and integration tests |
| `./gradlew :services:<name>:test` | Single service testing |

Integration tests use Testcontainers to spawn ephemeral Neo4j and PostgreSQL instances for zero-side-effect validation.

---

## Privacy and compliance

- **FERPA compliance**: Student identities are never stored in the contact graph.
- **Right to be forgotten**: Users can trigger complete data purging through the identity vault.
- **Temporal privacy**: All contact edges are automatically purged after 14 days.
