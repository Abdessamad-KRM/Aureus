# MyBank Project Structure

## 📁 Complete File Structure

```
Aureus/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/aureus/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── AccountDao.kt               # Room DAO for accounts
│   │   │   │   │   │   │   ├── TransactionDao.kt           # Room DAO for transactions
│   │   │   │   │   │   │   └── UserDao.kt                  # Room DAO for users
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   │   ├── AccountEntity.kt            # Room entity for accounts
│   │   │   │   │   │   │   ├── TransactionEntity.kt        # Room entity for transactions
│   │   │   │   │   │   │   └── UserEntity.kt               # Room entity for users
│   │   │   │   │   │   └── AppDatabase.kt                  # Room database configuration
│   │   │   │   │   │
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── api/
│   │   │   │   │   │   │   ├── AccountApiService.kt        # Retrofit API for accounts
│   │   │   │   │   │   │   ├── AuthApiService.kt           # Retrofit API for authentication
│   │   │   │   │   │   │   └── TransactionApiService.kt    # Retrofit API for transactions
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   │   ├── AccountResponse.kt          # API response DTO
│   │   │   │   │   │   │   ├── LoginRequest.kt             # Login request DTO
│   │   │   │   │   │   │   ├── LoginResponse.kt            # Login response DTO
│   │   │   │   │   │   │   ├── RegisterRequest.kt          # Register request DTO
│   │   │   │   │   │   │   ├── TransactionResponse.kt      # Transaction response DTO
│   │   │   │   │   │   │   └── UserResponse.kt             # User response DTO
│   │   │   │   │   │   └── RetrofitClient.kt               # Retrofit configuration
│   │   │   │   │   │
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── AccountRepositoryImpl.kt        # Account repository implementation
│   │   │   │   │       ├── AuthRepositoryImpl.kt           # Auth repository implementation
│   │   │   │   │       └── TransactionRepositoryImpl.kt    # Transaction repository implementation
│   │   │   │   │
│   │   │   │   ├── di/
│   │   │   │   │   ├── AppModule.kt                        # Hilt dependency injection module
│   │   │   │   │   └── ViewModelModule.kt                  # ViewModel dependency module
│   │   │   │   │
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Account.kt                      # Account domain model
│   │   │   │   │   │   ├── Resource.kt                     # Generic wrapper for data states
│   │   │   │   │   │   ├── Transaction.kt                  # Transaction domain model
│   │   │   │   │   │   └── User.kt                         # User domain model
│   │   │   │   │   │
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── AccountRepository.kt            # Account repository interface
│   │   │   │   │       ├── AuthRepository.kt               # Auth repository interface
│   │   │   │   │       └── TransactionRepository.kt        # Transaction repository interface
│   │   │   │   │
│   │   │   │   ├── notification/
│   │   │   │   │   └── MyFirebaseMessagingService.kt       # FCM service for push notifications
│   │   │   │   │
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── screen/
│   │   │   │   │   │   │   ├── LoginScreen.kt              # Login UI screen
│   │   │   │   │   │   │   └── RegisterScreen.kt           # Register UI screen
│   │   │   │   │   │   └── viewmodel/
│   │   │   │   │   │       └── AuthViewModel.kt            # Authentication ViewModel
│   │   │   │   │   │
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   ├── screen/
│   │   │   │   │   │   │   └── DashboardScreen.kt          # Dashboard UI screen
│   │   │   │   │   │   └── viewmodel/
│   │   │   │   │   │       └── DashboardViewModel.kt       # Dashboard ViewModel
│   │   │   │   │   │
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── Navigation.kt                   # App navigation configuration
│   │   │   │   │   │
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt                        # Color definitions
│   │   │   │   │   │   ├── Theme.kt                        # Theme configuration
│   │   │   │   │   │   └── Type.kt                         # Typography definitions
│   │   │   │   │   │
│   │   │   │   │   └── transaction/
│   │   │   │   │       ├── screen/
│   │   │   │   │       │   └── TransactionListScreen.kt    # Transaction list UI screen
│   │   │   │   │       └── viewmodel/
│   │   │   │   │           └── TransactionViewModel.kt     # Transaction ViewModel
│   │   │   │   │
│   │   │   │   ├── util/
│   │   │   │   │   ├── Constants.kt                        # App constants
│   │   │   │   │   └── SharedPreferencesManager.kt         # SharedPreferences wrapper
│   │   │   │   │
│   │   │   │   ├── MainActivity.kt                         # Main Activity
│   │   │   │   └── MyBankApplication.kt                    # Application class (Hilt)
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   └── strings.xml                         # String resources
│   │   │   │   ├── drawable/                               # Drawable resources
│   │   │   │   ├── mipmap/                                 # App icons
│   │   │   │   └── xml/                                    # XML resources
│   │   │   │
│   │   │   └── AndroidManifest.xml                         # Android manifest
│   │   │
│   │   ├── test/                                           # Unit tests
│   │   └── androidTest/                                    # Instrumented tests
│   │
│   ├── build.gradle.kts                                    # App-level Gradle config
│   ├── proguard-rules.pro                                  # ProGuard rules
│   └── google-services.json                                # Firebase configuration
│
├── gradle/
│   └── libs.versions.toml                                  # Dependency version catalog
│
├── build.gradle.kts                                        # Project-level Gradle config
├── settings.gradle.kts                                     # Gradle settings
├── README.md                                               # Project documentation
├── ARCHITECTURE.md                                         # Architecture documentation
└── PROJECT_STRUCTURE.md                                    # This file
```

## 📦 Package Organization

### Data Layer (`data/`)
**Purpose**: Handles data operations and persistence

#### `data/local/`
- **dao/**: Room Data Access Objects
  - Define database queries
  - Return Flow for reactive updates
  - Support CRUD operations

- **entity/**: Room Entities
  - Map to database tables
  - Include foreign key relationships
  - Support offline storage

- **AppDatabase.kt**: Room database configuration
  - Version management
  - DAO providers
  - Migration strategies

#### `data/remote/`
- **api/**: Retrofit API services
  - Define REST endpoints
  - Support suspend functions
  - Handle authentication headers

- **dto/**: Data Transfer Objects
  - API request/response models
  - JSON serialization
  - Mapping to domain models

- **RetrofitClient.kt**: Network configuration
  - Base URL configuration
  - Logging interceptor
  - Timeout settings

#### `data/repository/`
- Repository implementations
- Coordinate local and remote data
- Handle caching strategy
- Transform DTOs/Entities to domain models

### Domain Layer (`domain/`)
**Purpose**: Core business logic and contracts

#### `domain/model/`
- Pure business models
- No Android dependencies
- Used across all layers

#### `domain/repository/`
- Repository interfaces
- Define data operation contracts
- Used by ViewModels

### UI Layer (`ui/`)
**Purpose**: Presentation and user interaction

#### Feature-based organization:
- **auth/**: Authentication screens and ViewModels
- **dashboard/**: Main dashboard screens
- **transaction/**: Transaction-related screens
- **navigation/**: Navigation configuration
- **theme/**: UI theming

### Dependency Injection (`di/`)
**Purpose**: Manage dependencies with Hilt

- **AppModule.kt**: Application-level dependencies
  - Database
  - Repositories
  - API services
  - SharedPreferences

- **ViewModelModule.kt**: ViewModel dependencies
  - ViewModel factories
  - Repository injection

### Utilities (`util/`)
**Purpose**: Helper classes and constants

- **Constants.kt**: App-wide constants
- **SharedPreferencesManager.kt**: Preferences wrapper

### Notifications (`notification/`)
**Purpose**: Push notification handling

- Firebase Cloud Messaging service
- Notification creation and display
- Channel management

## 🎯 Key Files Explained

### Configuration Files

#### `build.gradle.kts` (App-level)
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

dependencies {
    // Networking
    implementation("com.squareup.retrofit2:retrofit")
    implementation("com.squareup.retrofit2:converter-gson")
    
    // Database
    implementation("androidx.room:room-runtime")
    ksp("androidx.room:room-compiler")
    
    // DI
    implementation("com.google.dagger:hilt-android")
    ksp("com.google.dagger:hilt-compiler")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
}
```

#### `libs.versions.toml`
Centralized dependency version management:
- Single source of truth for versions
- Easy version updates
- Shared across modules

#### `AndroidManifest.xml`
- App permissions
- Application class declaration
- Activity registration
- Service registration (FCM)
- Firebase metadata

### Core Application Files

#### `MyBankApplication.kt`
```kotlin
@HiltAndroidApp
class MyBankApplication : Application()
```
- Annotated with `@HiltAndroidApp`
- Triggers Hilt code generation
- App-level initialization

#### `MainActivity.kt`
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```
- Annotated with `@AndroidEntryPoint`
- Entry point for Compose UI
- ViewModel injection

### ProGuard Configuration

#### `proguard-rules.pro`
Critical rules for release builds:
- Keep data models for API/Database
- Keep Retrofit interfaces
- Obfuscate business logic
- Optimize code size

## 🔄 Data Flow Examples

### Login Flow
```
LoginScreen
    ↓ (user enters credentials)
AuthViewModel.login()
    ↓
AuthRepository.login()
    ↓
AuthApiService.login() → API call
    ↓
SharedPreferencesManager.saveToken()
    ↓
UserDao.insertUser() → Save to DB
    ↓
AuthViewModel updates loginState
    ↓
LoginScreen navigates to Dashboard
```

### Account List Flow
```
DashboardScreen
    ↓
DashboardViewModel.loadAccounts()
    ↓
AccountRepository.getAccounts()
    ↓ (check cache)
AccountDao.getAllAccounts() → Return Flow
    ↓ (background sync)
AccountApiService.getAccounts() → API call
    ↓
AccountDao.insertAccounts() → Update cache
    ↓
Flow emits new data
    ↓
DashboardViewModel updates state
    ↓
DashboardScreen recomposes with new data
```

## 🧩 Module Dependencies

```
UI Layer
    ↓ depends on
Domain Layer (interfaces)
    ↑ implemented by
Data Layer

Dependency Injection
    → provides instances to all layers
```

## 📝 Naming Conventions

### Files
- **Screens**: `*Screen.kt` (e.g., `LoginScreen.kt`)
- **ViewModels**: `*ViewModel.kt` (e.g., `AuthViewModel.kt`)
- **Repositories**: `*Repository.kt` / `*RepositoryImpl.kt`
- **DAOs**: `*Dao.kt` (e.g., `UserDao.kt`)
- **Entities**: `*Entity.kt` (e.g., `UserEntity.kt`)
- **DTOs**: `*Request.kt` / `*Response.kt`
- **APIs**: `*ApiService.kt`

### Packages
- Lowercase, no underscores
- Feature-based organization
- Clear separation of concerns

### Classes
- PascalCase
- Descriptive names
- Suffix indicates type (ViewModel, Repository, etc.)

### Functions
- camelCase
- Verb-based naming
- Clear intent (e.g., `getAccounts()`, `saveUser()`)

### Variables
- camelCase
- Descriptive names
- State variables prefixed with `_` for private mutable state

## 🚀 Getting Started with the Structure

### Adding a New Feature

1. **Create domain model** in `domain/model/`
2. **Create repository interface** in `domain/repository/`
3. **Create DTOs** in `data/remote/dto/`
4. **Create entities** in `data/local/entity/`
5. **Create DAO** in `data/local/dao/`
6. **Create API service** in `data/remote/api/`
7. **Implement repository** in `data/repository/`
8. **Create ViewModel** in `ui/[feature]/viewmodel/`
9. **Create Screen** in `ui/[feature]/screen/`
10. **Add to navigation** in `ui/navigation/Navigation.kt`
11. **Register in DI** in `di/AppModule.kt`

### Adding a New Screen

1. Create screen file in appropriate feature package
2. Create or use existing ViewModel
3. Define navigation route in `Navigation.kt`
4. Add composable to `NavHost`
5. Add navigation trigger from existing screen

## 📊 Statistics

- **Total Kotlin Files**: ~40
- **Total Packages**: ~15
- **Architecture Layers**: 3 (UI, Domain, Data)
- **Total Features**: 3 (Auth, Dashboard, Transactions)
- **Dependencies**: ~20 major libraries

## 🔍 Quick Reference

### Find a specific component:

| Component Type | Location |
|----------------|----------|
| API calls | `data/remote/api/` |
| Database queries | `data/local/dao/` |
| Business models | `domain/model/` |
| UI screens | `ui/[feature]/screen/` |
| State management | `ui/[feature]/viewmodel/` |
| Dependency injection | `di/` |
| Configuration | Root `build.gradle.kts` |
| String resources | `res/values/strings.xml` |

---

**Last Updated**: January 2026  
**Project**: MyBank Mobile Application  
**Course**: OFPPT Mobile Development