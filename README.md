# MyBank - Banking Mobile Application

## 📱 Description
MyBank is a modern Android banking application built with **Kotlin** and **Jetpack Compose**, following **MVVM** architecture and **Clean Architecture** principles.

## 🏗️ Architecture

### Clean Architecture Layers

```
├── domain/                      # Business Logic Layer
│   ├── model/                   # Domain Models
│   │   ├── User.kt
│   │   ├── Account.kt
│   │   ├── Transaction.kt
│   │   └── Resource.kt
│   └── repository/              # Repository Interfaces
│       ├── AuthRepository.kt
│       ├── AccountRepository.kt
│       └── TransactionRepository.kt
│
├── data/                        # Data Layer
│   ├── remote/                  # Remote Data Source
│   │   ├── api/                 # API Services
│   │   │   ├── AuthApiService.kt
│   │   │   ├── AccountApiService.kt
│   │   │   └── TransactionApiService.kt
│   │   ├── dto/                 # Data Transfer Objects
│   │   └── RetrofitClient.kt
│   ├── local/                   # Local Data Source
│   │   ├── entity/              # Room Entities
│   │   │   ├── UserEntity.kt
│   │   │   ├── AccountEntity.kt
│   │   │   └── TransactionEntity.kt
│   │   ├── dao/                 # Data Access Objects
│   │   │   ├── UserDao.kt
│   │   │   ├── AccountDao.kt
│   │   │   └── TransactionDao.kt
│   │   └── AppDatabase.kt
│   └── repository/              # Repository Implementations
│       ├── AuthRepositoryImpl.kt
│       ├── AccountRepositoryImpl.kt
│       └── TransactionRepositoryImpl.kt
│
├── ui/                          # Presentation Layer
│   ├── auth/
│   │   ├── screen/
│   │   │   ├── LoginScreen.kt
│   │   │   └── RegisterScreen.kt
│   │   └── viewmodel/
│   │       └── AuthViewModel.kt
│   ├── dashboard/
│   │   ├── screen/
│   │   │   └── DashboardScreen.kt
│   │   └── viewmodel/
│   │       └── DashboardViewModel.kt
│   ├── transaction/
│   │   ├── screen/
│   │   │   └── TransactionListScreen.kt
│   │   └── viewmodel/
│   │       └── TransactionViewModel.kt
│   ├── navigation/
│   │   └── Navigation.kt
│   └── theme/
│
├── di/                          # Dependency Injection
│   ├── AppModule.kt
│   └── ViewModelModule.kt
│
├── util/                        # Utilities
│   ├── SharedPreferencesManager.kt
│   └── Constants.kt
│
├── notification/                # Push Notifications
│   └── MyFirebaseMessagingService.kt
│
└── MyBankApplication.kt         # Application Class
```

## 🚀 Features

### ✅ Implemented
- **MVVM + Clean Architecture** structure
- **Authentication** (Login/Register with Firebase)
- **Account Management** (View accounts, balances)
- **Transaction History** (View transactions by account)
- **Offline Support** with Room Database
- **Push Notifications** with Firebase Cloud Messaging
- **State Management** with Kotlin Flow and StateFlow
- **Dependency Injection** with Hilt
- **ProGuard** configuration for code obfuscation
- **Repository Pattern** for data management
- **Jetpack Compose** modern UI

## 🛠️ Technologies & Libraries

### Core
- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI toolkit
- **MVVM Architecture** - Design pattern
- **Clean Architecture** - Project structure

### Networking
- **Retrofit** - REST API client
- **OkHttp** - HTTP client with logging
- **Gson** - JSON serialization

### Local Storage
- **Room** - Local database
- **SharedPreferences** - Lightweight data persistence

### Dependency Injection
- **Hilt** - Dependency injection framework

### Asynchronous
- **Coroutines** - Asynchronous programming
- **Flow** - Reactive data streams

### Firebase
- **Firebase Authentication** - User authentication
- **Firebase Cloud Messaging** - Push notifications

### Security
- **ProGuard/R8** - Code obfuscation and optimization

### UI
- **Material Design 3** - Modern design system
- **Jetpack Navigation** - Navigation component for Compose

## 📋 Prerequisites

1. Android Studio Ladybug or newer
2. JDK 11 or higher
3. Android SDK (min API 26, target API 36)
4. Firebase project setup

## 🔧 Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Aureus
```

### 2. Configure Firebase
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing one
3. Add Android app with package name: `com.example.aureus`
4. Download `google-services.json`
5. Place it in `app/` directory (replace the placeholder file)

### 3. Configure API Base URL
Update the base URL in:
- `data/remote/RetrofitClient.kt`
- `util/Constants.kt`

Replace `https://api.mybank.test/` with your actual API URL.

### 4. Sync and Build
```bash
./gradlew build
```

### 5. Run the App
```bash
./gradlew installDebug
```

## 📱 App Structure

### Authentication Flow
1. **Login Screen** - User authentication
2. **Register Screen** - New user registration
3. **Token Management** - Secure token storage

### Dashboard Flow
1. **Account List** - Display all user accounts
2. **Total Balance** - Aggregate balance view
3. **Offline Support** - Cached data display

### Transaction Flow
1. **Transaction List** - View account transactions
2. **Transaction Details** - Type, amount, merchant
3. **Real-time Sync** - Auto-refresh capability

## 🔒 Security Features

### ProGuard Configuration
- Code obfuscation enabled
- API models protected
- Sensitive classes kept for debugging
- Optimized APK size

### Data Security
- Encrypted SharedPreferences (can be enhanced)
- Secure token storage
- HTTPS-only communication
- Firebase Authentication

## 🔔 Push Notifications

### Notification Types
1. **Transaction Notifications** - New transaction alerts
2. **Low Balance Alerts** - Balance threshold warnings
3. **Info Notifications** - General information

### FCM Setup
Service registered in `MyFirebaseMessagingService.kt`
- Handles incoming notifications
- Creates notification channels
- Manages notification display

## 📦 Dependencies

See `gradle/libs.versions.toml` for complete dependency list.

Key dependencies:
- Retrofit: 2.9.0
- Room: 2.6.1
- Hilt: 2.51.1
- Coroutines: 1.9.0
- Compose BOM: 2024.09.00

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

## 📝 API Integration

### Required Endpoints
```
POST   /auth/login
POST   /auth/register
POST   /auth/logout
GET    /auth/me
GET    /accounts
GET    /accounts/{id}
GET    /transactions
GET    /accounts/{id}/transactions
```

### API Response Format
All endpoints should return JSON with standard structure.
See DTOs in `data/remote/dto/` for expected formats.

## 🚧 TODO / Future Enhancements

- [ ] Add biometric authentication
- [ ] Implement money transfer feature
- [ ] Add transaction filters and search
- [ ] Create transaction categories
- [ ] Add dark/light theme toggle
- [ ] Implement PIN protection
- [ ] Add account statements PDF export
- [ ] Create spending analytics dashboard
- [ ] Add multi-language support
- [ ] Implement wear OS companion app

## 📄 License

This project is created for educational purposes as part of OFPPT Mobile Development course.

## 👥 Contributors


## 📞 Support

For issues and questions, please contact your instructor or create an issue in the repository.

---

**Note**: This is a learning project. Replace placeholder values (API URLs, Firebase config) with actual values before deployment.