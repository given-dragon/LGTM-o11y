# Caro Demo - Spring Modulith Example Project

This project simulates a Flashcard learning platform (like Anki) built with **Spring Boot** and **Kotlin**, demonstrating a production-ready **Modular Monolith** architecture.

It strictly follows **Spring Modulith** principles and **Clean Architecture** to ensure loose coupling and high cohesion between business modules.

## 🏗 Architecture Principles

### 1. Spring Modulith & Encapsulation
Unlike traditional Layered Architecture (Controller -> Service -> Repository), we organize code by **Business Domain** (Modules).

- **Strict Boundaries:** Code that belongs to a specific domain resides in its own package (`com.caro.workbook`, `com.caro.review`, etc.).
- **Information Hiding:**
    - **Public API:** Only `Service Interfaces`, `DTOs`, `Events`, and `Exceptions` are public.
    - **Internal Implementation:** Implementation details (`ServiceImpl`, `Repository`, `Entity`, `Controller`) are hidden inside the `internal` package.
- **Named Interfaces:** We use `package-info.java` with `@NamedInterface` to explicitly define allowed interaction points (`event`, `exception`).

### 2. Standardized Package Structure

Every module in this project follows this strict directory structure:

```text
com.caro.[module]
├── [Module]Service.kt       (✅ API: The only entry point for other modules)
├── [Module]Dto.kt           (✅ API: Read-only Data Transfer Objects)
├── package-info.java        (✅ Config: Defines Named Interfaces)
│
├── exception                (📢 Public API: Business Exceptions)
│   ├── [Module]Exception.kt
│   └── package-info.java    (Exposed as "exception")
│
├── event                    (📢 Public API: Domain Events)
│   ├── [EventName]Event.kt
│   └── package-info.java    (Exposed as "event")
│
└── internal                 (🔒 Private: Implementation Details)
    ├── [Module]ServiceImpl.kt
    ├── [Module]Repository.kt
    ├── [Module]Entity.kt
    │
    ├── event                (👂 Internal: Event Listeners)
    │   └── [Task]EventListener.kt
    │
    └── web                  (🌍 Web: REST Controllers & Request DTOs)
        ├── [Module]Controller.kt
        └── [Module]WebDtos.kt
```

## 📦 Core Modules

| Module | Description | Dependencies |
| :--- | :--- | :--- |
| **Workbook** | Manages Decks and Cards. The Core domain. | `Member`, `Shared` |
| **Review** | Handles Spaced Repetition (SR) algorithms (SM-2) and review logs. Publishes `CardReviewedEvent`. | `Shared` |
| **Ingestion** | Handles AI-based card generation from images/files. | `Workbook`, `Shared` |
| **Gamification**| Manages user streaks, EXP, and badges. Subscribes to `CardReviewedEvent`. | `Review`, `Shared` |
| **Analytics** ✨ | Tracks daily study statistics. Subscribes to `CardReviewedEvent`. | `Review`, `Shared` |
| **Notification** ✨ | Handles goal achievement alerts. Subscribes to `CardReviewedEvent`. | `Review`, `Analytics`, `Shared` |
| **Member** | User management and profiles. | `Shared` |
| **Shared** | Common infrastructure (Security, Global Error Handling, Swagger, Async Config). | None |

## 💡 Key Design Decisions

### Command vs DTO vs Web Request
We strictly separate data structures based on their layer and intent:
1.  **Web Request (Internal):** Defined in `internal/web/*Requests.kt`. Used only for deserializing JSON requests in Controllers. Validation annotations (`@NotBlank`) belong here.
2.  **Command (Public):** Used when one module requests an action from another (e.g., `CreateCardCommand`). Independent of HTTP/Web concerns.
3.  **Response DTO (Public):** Used for reading data. Pure data containers.

### Event-Driven Interaction
Modules do not directly change each other's state to avoid tight coupling. Instead, they emit **Domain Events**.

*   **Example:** When a User reviews a card, `Review` module publishes `CardReviewedEvent`.
*   **Listener:** `Gamification` module listens to this event to update Streaks/EXP (in `internal/event`).

## 🛠 Tech Stack

- **Language:** Kotlin (primary), Java (package-info)
- **Framework:** Spring Boot 3.x
- **Architecture:** Spring Modulith
- **Database:** H2 (In-Memory) / JPA
- **Build Tool:** Gradle (Kotlin DSL)

## ✅ How to Verify Modularity

Run the tests to verify module boundaries are respected:

```bash
./gradlew clean test
```
