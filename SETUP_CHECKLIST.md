# MyBank - Setup Checklist

## ✅ Pre-Development Setup

### 1. Development Environment
- [ ] Android Studio installed (Ladybug or newer)
- [ ] JDK 11 or higher installed
- [ ] Android SDK configured (API 26-36)
- [ ] Emulator or physical device available
- [ ] Git installed and configured

### 2. Firebase Configuration
- [ ] Firebase account created
- [ ] New Firebase project created
- [ ] Android app added to Firebase project
  - Package name: `com.example.aureus`
- [ ] `google-services.json` downloaded
- [ ] `google-services.json` placed in `app/` directory
- [ ] Firebase Authentication enabled
  - Email/Password sign-in method activated
- [ ] Firebase Cloud Messaging configured (default enabled)

### 3. Backend API Setup
Choose one option:

#### Option A: MockAPI (Recommended for Learning)
- [ ] MockAPI account created at https://mockapi.io
- [ ] Project created
- [ ] Endpoints created:
  - [ ] POST `/auth/login`
  - [ ] POST `/auth/register`
  - [ ] GET `/accounts`
  - [ ] GET `/transactions`
- [ ] Base URL copied
- [ ] Base URL updated in `RetrofitClient.kt`
- [ ] Base URL updated in `Constants.kt`

#### Option B: JSON Server (Local Testing)
- [ ] Node.js installed
- [ ] JSON Server installed: `npm install -g json-server`
- [ ] `db.json` file created with mock data
- [ ] Server started: `json-server --watch db.json --port 3000`
- [ ] Base URL set to `http://10.0.2.2:3000/` (for emulator)

#### Option C: Real Backend
- [ ] Backend server deployed
- [ ] All required endpoints implemented
- [ ] HTTPS configured (recommended)
- [ ] Base URL updated in code

### 4. Project Configuration
- [ ] Project cloned/opened in Android Studio
- [ ] Gradle sync completed successfully
- [ ] No build errors
- [ ] Dependencies downloaded

### 5. Code Updates Required
- [ ] Update base URL in `data/remote/RetrofitClient.kt`:
  ```kotlin
  private const val BASE_URL = "YOUR_API_URL_HERE"
  ```
- [ ] Update base URL in `util/Constants.kt`:
  ```kotlin
  const val BASE_URL = "YOUR_API_URL_HERE"
  ```
- [ ] Verify package name matches Firebase configuration

## 🧪 Testing Checklist

### Build Tests
- [ ] Clean build succeeds: `./gradlew clean`
- [ ] Debug build succeeds: `./gradlew assembleDebug`
- [ ] Release build succeeds: `./gradlew assembleRelease`
- [ ] No lint errors: `./gradlew lint`

### Functionality Tests

#### Authentication
- [ ] Login screen displays correctly
- [ ] Register screen displays correctly
- [ ] Form validation works
- [ ] Login with valid credentials succeeds
- [ ] Login with invalid credentials fails appropriately
- [ ] Registration with valid data succeeds
- [ ] Token is saved after successful login
- [ ] User stays logged in after app restart

#### Dashboard
- [ ] Dashboard loads after login
- [ ] Account list displays
- [ ] Total balance calculates correctly
- [ ] Pull to refresh works
- [ ] Clicking account navigates to transactions
- [ ] Logout works correctly

#### Transactions
- [ ] Transaction list loads for selected account
- [ ] Transactions display with correct formatting
- [ ] Credit transactions show positive
- [ ] Debit transactions show negative
- [ ] Back navigation works
- [ ] Pull to refresh works

#### Offline Mode
- [ ] App works without internet (cached data)
- [ ] Data persists across app restarts
- [ ] Sync happens when connection restored

#### Notifications
- [ ] Firebase service initialized
- [ ] Notification channels created
- [ ] Test notification received from Firebase Console
- [ ] Notification opens app when clicked

## 🔒 Security Checklist

### Development
- [ ] `usesCleartextTraffic` set to `true` only for development
- [ ] API keys not hardcoded in source
- [ ] `.gitignore` includes `google-services.json` (if contains secrets)
- [ ] Debug logs removed from sensitive operations

### Production (Before Release)
- [ ] `usesCleartextTraffic` set to `false`
- [ ] ProGuard enabled
- [ ] Code obfuscation tested
- [ ] All API keys moved to secure location
- [ ] Certificate pinning implemented (recommended)
- [ ] App signing configured

## 📱 Device Testing Checklist

### Minimum Requirements
- [ ] Tested on API 26 (Android 8.0)
- [ ] Tested on API 33 (Android 13)
- [ ] Tested on API 34+ (Android 14+)

### Screen Sizes
- [ ] Tested on phone (small)
- [ ] Tested on phone (large)
- [ ] Tested on tablet (optional)

### Orientations
- [ ] Portrait mode works
- [ ] Landscape mode works (optional)

### Scenarios
- [ ] Fresh install
- [ ] Update from previous version
- [ ] Low memory conditions
- [ ] Slow network
- [ ] No network
- [ ] Background/Foreground transitions
- [ ] App restart
- [ ] System reboot

## 📝 Documentation Checklist

### Code Documentation
- [ ] All public classes documented
- [ ] All public methods documented
- [ ] Complex logic has comments
- [ ] TODO items tracked

### Project Documentation
- [ ] README.md reviewed
- [ ] ARCHITECTURE.md reviewed
- [ ] PROJECT_STRUCTURE.md reviewed
- [ ] IMPLEMENTATION_GUIDE.md reviewed
- [ ] SETUP_CHECKLIST.md (this file) completed

## 🎓 Course Requirements Checklist

### Architecture (MVVM)
- [x] Model layer implemented
- [x] View layer implemented (Compose)
- [x] ViewModel layer implemented
- [x] Repository pattern used
- [x] Clean architecture followed

### Technologies Required

#### Networking
- [x] Retrofit configured
- [x] OkHttp logging interceptor
- [x] Gson converter
- [x] API services defined

#### Local Storage
- [x] Room database configured
- [x] Entities defined
- [x] DAOs implemented
- [x] SharedPreferences used for token

#### Firebase
- [ ] Firebase Authentication integrated (setup required)
- [x] Firebase Cloud Messaging implemented
- [ ] Push notifications working (testing required)

#### Security
- [x] ProGuard rules configured
- [x] Code obfuscation enabled for release
- [x] Sensitive data handling

### Features Required

#### Core Features
- [x] User authentication (login/register)
- [x] Account list view
- [x] Transaction history
- [x] Balance display
- [x] Offline support
- [x] Push notifications

#### Additional Features
- [x] Pull to refresh
- [x] Navigation between screens
- [x] Error handling
- [x] Loading states
- [x] Material Design 3 UI

## 🚀 Deployment Checklist

### Pre-Deployment
- [ ] All features tested
- [ ] No critical bugs
- [ ] Performance acceptable
- [ ] Security reviewed
- [ ] Legal compliance checked (privacy policy, terms)

### Build Configuration
- [ ] Version code incremented
- [ ] Version name updated
- [ ] App signing configured
- [ ] ProGuard tested with release build
- [ ] APK/AAB size acceptable

### Store Preparation (If Publishing)
- [ ] App icon designed
- [ ] Screenshots captured
- [ ] App description written
- [ ] Privacy policy created
- [ ] Store listing prepared

## 📊 Performance Checklist

### App Performance
- [ ] App starts in < 3 seconds
- [ ] Screens load quickly
- [ ] No ANR (Application Not Responding)
- [ ] No memory leaks
- [ ] Battery usage acceptable

### Network Performance
- [ ] API calls are efficient
- [ ] Caching implemented
- [ ] Retry logic for failed requests
- [ ] Timeout handling

### Database Performance
- [ ] Queries are optimized
- [ ] Indexes used where needed
- [ ] No blocking main thread
- [ ] Efficient data sync

## 🎯 Final Review Checklist

### Before Submission
- [ ] All requirements met
- [ ] Code is clean and organized
- [ ] Documentation is complete
- [ ] App works end-to-end
- [ ] No hardcoded credentials
- [ ] No debug/test code in production
- [ ] README updated with any changes
- [ ] All files committed to repository

### Presentation Preparation
- [ ] Demo flow prepared
- [ ] Edge cases handled
- [ ] Screenshots/video ready
- [ ] Architecture diagram available
- [ ] Q&A preparation done

## 📞 Support Resources

### When Stuck
1. [ ] Check documentation in this repo
2. [ ] Review Android official documentation
3. [ ] Search Stack Overflow
4. [ ] Ask classmates
5. [ ] Contact instructor: nizar.ettaheri@ofppt.ma

### Useful Commands
```bash
# Clean build
./gradlew clean

# Build debug
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Run tests
./gradlew test

# Check lint
./gradlew lint

# Build release
./gradlew assembleRelease
```

---

## ✨ Quick Start

### Minimum Required Steps to Run App:

1. **Setup Firebase** (15 min)
   - Create project
   - Add Android app
   - Download google-services.json
   - Enable Email/Password auth

2. **Setup Backend** (10 min)
   - Create MockAPI account
   - Create basic endpoints
   - Copy base URL

3. **Update Code** (5 min)
   - Replace google-services.json
   - Update base URL in 2 files

4. **Build & Run** (5 min)
   - Sync Gradle
   - Build project
   - Run on emulator/device

**Total Time**: ~35 minutes to get app running!

---

**Status**: Ready for Setup  
**Last Updated**: January 2026  
**Next Step**: Follow "Quick Start" above