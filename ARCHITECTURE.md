# MyBank - Architecture Documentation

## 📐 Architecture Overview

This project follows **MVVM (Model-View-ViewModel)** pattern combined with **Clean Architecture** principles for a scalable, maintainable, and testable Android application.

## 🏛️ Architecture Layers

### 1. Presentation Layer (UI)
**Location**: `ui/`

**Responsibility**: Display data and handle user interactions

**Components**:
- **Screens (Composables)**: UI components built with Jetpack Compose
- **ViewModels**: Manage UI state and business logic
- **Navigation**: Handle screen navigation flow

**Key Features**:
- Reactive UI with Compose
- State management with StateFlow
- Navigation with Jetpack Navigation Compose

```kotlin
// Example ViewModel structure
class DashboardViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _accountsState = MutableStateFlow<Resource<List<Account>>>(Resource.Loading)
    val accountsState: StateFlow<Resource<List<Account>>> = _accountsState.asStateFlow()
}
```

### 2. Domain Layer
**Location**: `domain/`

**Responsibility**: Core business logic and rules

**Components**:
- **Models**: Pure business entities
- **Repository Interfaces**: Abstract data operations
- **Use Cases** (to be implemented): Business logic operations

**Key Features**:
- Framework-independent
- Contains business rules
- Defines contracts for data operations

```kotlin
// Example Domain Model
data class Account(
    val id: String,
    val accountNumber: String,
    val accountType: String,
    val balance: Double,
    val currency: String,
    val isActive: Boolean
)
```

### 3. Data Layer
**Location**: `data/`

**Responsibility**: Data management and persistence

**Components**:

#### 3.1 Remote Data Source
- **API Services**: Retrofit interfaces for network calls
- **DTOs**: Data transfer objects for API responses
- **Retrofit Client**: Network configuration

#### 3.2 Local Data Source
- **Room Database**: Local SQLite database
- **DAOs**: Data access objects for database operations
- **Entities**: Database table models

#### 3.3 Repository Implementations
- Implement domain repository interfaces
- Coordinate between remote and local data sources
- Handle data caching and synchronization

```kotlin
// Example Repository Implementation
class AccountRepositoryImpl(
    private val accountDao: AccountDao,
    private val accountApi: AccountApiService
) : AccountRepository {
    override suspend fun getAccounts(forceRefresh: Boolean): Flow<List<Account>> {
        if (forceRefresh) {
            // Fetch from API and cache
            refreshAccounts()
        }
        // Return cached data
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
}
```

## 🔄 Data Flow

### Normal Flow (Success Case)
```
User Action
    ↓
View (Composable)
    ↓
ViewModel
    ↓
Repository Interface (Domain)
    ↓
Repository Implementation (Data)
    ↓
Data Source (Remote/Local)
    ↓
Repository Implementation
    ↓
ViewModel (State Update)
    ↓
View (UI Update)
```

### Offline-First Flow
```
Request Data
    ↓
Repository checks local cache
    ├─ Has Data? → Return cached data + Sync in background
    └─ No Data? → Fetch from API → Save to cache → Return data
```

## 🎯 Design Patterns

### 1. Repository Pattern
**Purpose**: Abstract data sources from business logic

**Implementation**:
- Domain layer defines repository interfaces
- Data layer provides concrete implementations
- ViewModels depend on abstractions, not implementations

**Benefits**:
- Easy to test (mock repositories)
- Flexible data source switching
- Centralized data management

### 2. Dependency Injection (Hilt)
**Purpose**: Manage dependencies and promote loose coupling

**Implementation**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        userDao: UserDao,
        preferencesManager: SharedPreferencesManager
    ): AuthRepository {
        return AuthRepositoryImpl(userDao, preferencesManager)
    }
}
```

**Benefits**:
- Automatic dependency resolution
- Lifecycle-aware injection
- Easy testing with mock dependencies

### 3. Observer Pattern (Reactive Streams)
**Purpose**: Reactive data updates

**Implementation**:
- Room returns `Flow<T>` for database queries
- ViewModels expose `StateFlow<T>` for UI state
- UI observes state changes and recomposes

**Benefits**:
- Automatic UI updates
- Efficient data propagation
- Backpressure handling

### 4. Single Source of Truth
**Purpose**: Maintain data consistency

**Implementation**:
- Local database is the SSOT
- Remote data updates local cache
- UI always reads from local database

**Benefits**:
- Offline capability
- Consistent data state
- Simplified synchronization

## 📊 Component Responsibilities

### ViewModel
✅ **Should**:
- Hold UI state
- Handle user interactions
- Call repository methods
- Transform data for UI
- Manage coroutine scopes

❌ **Should NOT**:
- Access Android framework classes (except lifecycle-aware)
- Perform direct network/database operations
- Hold references to Views/Contexts

### Repository
✅ **Should**:
- Coordinate data sources
- Implement caching strategy
- Handle data transformation (DTO ↔ Entity ↔ Domain Model)
- Manage data synchronization
- Return domain models

❌ **Should NOT**:
- Contain business logic
- Hold UI state
- Know about ViewModels

### Data Source (DAO/API)
✅ **Should**:
- Perform CRUD operations
- Return raw data
- Handle data access errors

❌ **Should NOT**:
- Implement business logic
- Perform data transformation
- Cache data (Repository's job)

## 🔐 Security Architecture

### Authentication Flow
```
1. User enters credentials
2. Login API call with credentials
3. Receive JWT token
4. Store token in SharedPreferences (encrypted recommended)
5. Inject token in subsequent API requests via Interceptor
6. On 401 error → Refresh token or logout
```

### Data Security Layers
1. **Network**: HTTPS only, certificate pinning (recommended)
2. **Storage**: Encrypted SharedPreferences, Room encryption (recommended)
3. **Code**: ProGuard obfuscation for release builds
4. **Authentication**: Firebase Authentication + JWT tokens

## 🔔 Notification Architecture

### FCM Integration
```
Firebase Cloud Messaging
    ↓
MyFirebaseMessagingService.onMessageReceived()
    ↓
Parse notification type
    ├─ Transaction → Show transaction notification
    ├─ Low Balance → Show alert notification
    └─ Info → Show general notification
    ↓
Update local database (optional)
    ↓
Notify user
```

### Notification Channels
- **Transaction Channel**: High priority, sound + vibration
- **Alerts Channel**: High priority, persistent
- **Info Channel**: Normal priority

## 🧪 Testing Strategy

### Unit Tests
- **ViewModels**: Test state management and business logic
- **Repositories**: Test data coordination (mock DAOs and APIs)
- **Use Cases**: Test business rules

### Integration Tests
- **Database**: Test Room DAOs with in-memory database
- **API**: Test Retrofit services with MockWebServer

### UI Tests
- **Screens**: Test user interactions with Compose Testing
- **Navigation**: Test screen transitions

## 🔧 Build Configuration

### Build Types
1. **Debug**:
   - Minification disabled
   - Logging enabled
   - Test endpoints allowed

2. **Release**:
   - ProGuard enabled
   - Resource shrinking
   - Code obfuscation
   - Production endpoints only

### ProGuard Rules
- Keep data models (DTOs, Entities)
- Keep Retrofit interfaces
- Keep Room entities and DAOs
- Obfuscate business logic

## 📱 Offline Support Strategy

### Cache-First Approach
```kotlin
suspend fun getAccounts(forceRefresh: Boolean = false): Flow<List<Account>> {
    // 1. Always return cached data first
    val cachedData = accountDao.getAllAccounts()
    
    // 2. Refresh in background if needed
    if (forceRefresh || isCacheStale()) {
        try {
            val remoteData = accountApi.getAccounts(token)
            accountDao.insertAccounts(remoteData)
        } catch (e: Exception) {
            // Continue with cached data
        }
    }
    
    return cachedData.map { it.toDomainModel() }
}
```

### Sync Strategy
- **Manual Sync**: User-triggered refresh
- **Auto Sync**: Background sync when app opens
- **Conflict Resolution**: Server data always wins (for this app)

## 🚀 Performance Optimization

### Database
- Use indices on foreign keys
- Implement pagination for large lists
- Use transactions for bulk operations

### Network
- Enable HTTP caching
- Implement request debouncing
- Use efficient JSON parsing (Gson)

### UI
- Lazy loading with LazyColumn
- Avoid unnecessary recomposition
- Use remember and derivedStateOf

## 📈 Scalability Considerations

### Future Enhancements Ready
- **Use Cases Layer**: Can be added between ViewModel and Repository
- **Feature Modules**: Structure supports modularization
- **Multi-platform**: Domain layer is framework-agnostic
- **Microservices**: Repository pattern allows multiple API sources

### Design Decisions
- **Flow over LiveData**: Better for coroutines and compose
- **Hilt over Dagger**: Simpler setup, less boilerplate
- **Retrofit over Ktor**: More mature, better Android support
- **Room over SQLDelight**: Better IDE support, official Jetpack library

## 📚 References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [MVVM Pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel)
- [Jetpack Compose Guidelines](https://developer.android.com/jetpack/compose)

---

**Last Updated**: January 2026  
**Version**: 1.0  
**Author**: OFPPT Mobile Development Course