# Dial-112 Android - Implementation Plan

**Goal:** Complete the Android app with backend integration
**Timeline:** 10 weeks (can be accelerated with focused effort)
**Current Status:** 30% → Target: 100%

---

## 🎯 Sprint 1: Critical Fixes (Week 1)

### Priority 1: Fix Manifest & App Crashes ⚠️

**Tasks:**
1. ✅ Create `Dial112Application.kt` class
2. ✅ Create `SOSForegroundService.kt`
3. ✅ Register all 12 missing activities in `AndroidManifest.xml`
4. ✅ Fix Maps API key configuration
5. ✅ Test app launches without crashing

**Files to Create:**
- `app/src/main/java/com/dial112/Dial112Application.kt`
- `app/src/main/java/com/dial112/service/SOSForegroundService.kt`

**Files to Modify:**
- `app/src/main/AndroidManifest.xml`

---

## 🎯 Sprint 2: Data Layer Architecture (Week 1-2)

### Priority 2: Network & Data Infrastructure

**Tasks:**
1. ✅ Create API service layer
2. ✅ Create Retrofit client with interceptors
3. ✅ Define all data models matching backend API
4. ✅ Configure Hilt dependency injection
5. ✅ Create repository pattern
6. ✅ Add error handling framework

**Package Structure to Create:**
```
com.dial112/
├── data/
│   ├── api/
│   │   ├── ApiService.kt
│   │   ├── ApiClient.kt
│   │   └── interceptor/
│   │       └── AuthInterceptor.kt
│   ├── model/
│   │   ├── request/
│   │   │   ├── LoginRequest.kt
│   │   │   ├── RegisterRequest.kt
│   │   │   ├── CreateCaseRequest.kt
│   │   │   └── SOSRequest.kt
│   │   └── response/
│   │       ├── LoginResponse.kt
│   │       ├── CaseResponse.kt
│   │       └── SOSResponse.kt
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── CaseRepository.kt
│   │   ├── SOSRepository.kt
│   │   └── PoliceRepository.kt
│   └── local/
│       ├── PreferencesManager.kt
│       └── TokenManager.kt
├── domain/
│   └── usecase/
│       ├── LoginUseCase.kt
│       ├── TriggerSOSUseCase.kt
│       └── CreateCaseUseCase.kt
├── di/
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
└── util/
    ├── Resource.kt (sealed class for API states)
    ├── NetworkResult.kt
    └── Constants.kt
```

---

## 🎯 Sprint 3: Authentication Flow (Week 2)

### Priority 3: User Authentication

**Tasks:**
1. ✅ Integrate LoginActivity with backend
2. ✅ Implement SignupActivity with API
3. ✅ Add OTPVerificationActivity logic
4. ✅ Implement Forgot Password flow
5. ✅ Create ProfileSettingsActivity functionality
6. ✅ Add token management (JWT)
7. ✅ Implement secure storage (EncryptedSharedPreferences)

**Backend Endpoints to Use:**
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/auth/profile`
- `PUT /api/auth/profile`

**Files to Update:**
- `LoginActivity.kt` - Add ViewModel + API integration
- `SignupActivity.kt` - Complete registration flow
- `OTPVerificationActivity.kt` - OTP validation
- `ProfileSettingsActivity.kt` - Profile CRUD

---

## 🎯 Sprint 4: Citizen Features (Week 3)

### Priority 4: Core Citizen Functionality

**Tasks:**
1. ✅ Create FileFIRActivity + layout
2. ✅ Implement CaseHistoryActivity with API
3. ✅ Complete CitizenHomeActivity with real data
4. ✅ Integrate SOSActivity with backend
5. ✅ Add NotificationsActivity functionality
6. ✅ Complete EmergencyContactsActivity
7. ✅ Implement MapLocationActivity with crime data

**Backend Endpoints:**
- `POST /api/cases` - File new FIR
- `GET /api/cases` - Get user's cases
- `GET /api/cases/:id` - Case details
- `POST /api/sos` - Trigger SOS
- `GET /api/sos/history` - SOS history

**New Activities to Create:**
- `FileFIRActivity.kt` + `activity_file_fir.xml`
- `CrimeMapActivity.kt` (complete the existing `MapLocationActivity`)

**Files to Update:**
- `CitizenHomeActivity.kt` - Load real cases
- `CaseHistoryActivity.kt` - List all cases
- `CaseDetailsActivity.kt` - Show case details
- `SOSActivity.kt` - Backend integration

---

## 🎯 Sprint 5: Police Features (Week 4)

### Priority 5: Police Officer Functionality

**Tasks:**
1. ✅ Complete CaseManagementActivity with filters
2. ✅ Implement ActiveSOSActivity
3. ✅ Add CriminalSearchActivity
4. ✅ Create PCRVanTrackingActivity
5. ✅ Complete CaseDetailsActivity for police
6. ✅ Implement case assignment logic

**Backend Endpoints:**
- `GET /api/sos/active` - Active SOS alerts
- `POST /api/sos/:id/assign` - Respond to SOS
- `GET /api/criminals` - Search criminals
- `GET /api/criminals/:id` - Criminal details
- `GET /api/pcr-vans` - PCR van locations
- `PUT /api/cases/:id/status` - Update case status

**Files to Update:**
- `PoliceHomeActivity.kt` - Real-time SOS alerts
- `CaseManagementActivity.kt` - Complete filters
- `CameraScannerActivity.kt` - Backend AI integration

**New Activities to Create:**
- `CriminalSearchActivity.kt` + layout
- `PCRVanTrackingActivity.kt` + layout

---

## 🎯 Sprint 6: AI Integration (Week 5)

### Priority 6: AI-Powered Features

**Tasks:**
1. ✅ Integrate ChatActivity with AI backend
2. ✅ Connect face recognition to AI service
3. ✅ Implement ANPR with backend
4. ✅ Add weapon detection integration
5. ✅ Create real-time processing feedback

**Backend Endpoints:**
- `POST /api/ai/chat` - AI chatbot
- `POST /api/ai/face-recognition` - Face scan
- `POST /api/ai/anpr` - License plate
- `POST /api/ai/weapon-detection` - Weapon scan

**Files to Update:**
- `ChatActivity.kt` - Real AI responses
- `CameraScannerActivity.kt` - Upload images to AI service

---

## 🎯 Sprint 7: Real-time Features (Week 6)

### Priority 7: Location & Tracking

**Tasks:**
1. ✅ Implement real-time location tracking
2. ✅ Add SOS foreground service
3. ✅ Create geofencing for PCR vans
4. ✅ Implement WebSocket for live updates
5. ✅ Add Google Maps integration
6. ✅ Crime heatmap visualization

**Files to Complete:**
- `SOSForegroundService.kt` - Background location
- `MapLocationActivity.kt` - Crime map
- PCR van live tracking

---

## 🎯 Sprint 8: Polish & UX (Week 7)

### Priority 8: User Experience

**Tasks:**
1. ✅ Add loading states for all API calls
2. ✅ Implement error handling UI
3. ✅ Add empty states for lists
4. ✅ Network connectivity detection
5. ✅ Offline mode with Room database
6. ✅ Pull-to-refresh everywhere
7. ✅ Skeleton loaders

**Files to Create:**
- `util/LoadingStateHandler.kt`
- `util/NetworkMonitor.kt`
- Custom empty state views

---

## 🎯 Sprint 9: Admin & Advanced (Week 8)

### Priority 9: Admin Dashboard

**Tasks:**
1. ✅ Complete AdminDashboardActivity
2. ✅ Analytics visualization
3. ✅ User management
4. ✅ System configuration
5. ✅ Reports generation

**Files to Update:**
- `AdminDashboardActivity.kt`

---

## 🎯 Sprint 10: Testing & Deployment (Week 9-10)

### Priority 10: Quality Assurance

**Tasks:**
1. ✅ Unit tests for repositories
2. ✅ ViewModel tests
3. ✅ UI tests for critical flows
4. ✅ Integration tests
5. ✅ Security audit
6. ✅ Performance optimization
7. ✅ ProGuard configuration
8. ✅ Release build setup
9. ✅ Play Store assets

---

## 📋 Adapters Needed

Create these RecyclerView adapters:

1. ✅ `CaseAdapter.kt` - For case lists
2. ✅ `SOSAlertAdapter.kt` - For active SOS
3. ✅ `CriminalAdapter.kt` - For search results
4. ✅ `PCRVanAdapter.kt` - For van tracking
5. ✅ `EvidenceAdapter.kt` - For case evidence
6. ✅ `TimelineAdapter.kt` - For case updates

---

## 🔧 Immediate Next Steps (Start Today)

### Step 1: Fix App Crashes
```kotlin
// Create Dial112Application.kt
// Configure Hilt
// Fix manifest
```

### Step 2: Setup Network Layer
```kotlin
// Create ApiService with all endpoints
// Setup Retrofit + OkHttp
// Add interceptors
```

### Step 3: First Feature
```kotlin
// Complete Login → CitizenHome flow
// With real backend integration
```

---

## 📊 Progress Tracking

- [ ] Sprint 1: Critical Fixes (0/5 tasks)
- [ ] Sprint 2: Data Layer (0/6 tasks)
- [ ] Sprint 3: Authentication (0/7 tasks)
- [ ] Sprint 4: Citizen Features (0/7 tasks)
- [ ] Sprint 5: Police Features (0/6 tasks)
- [ ] Sprint 6: AI Integration (0/5 tasks)
- [ ] Sprint 7: Real-time (0/6 tasks)
- [ ] Sprint 8: Polish (0/7 tasks)
- [ ] Sprint 9: Admin (0/5 tasks)
- [ ] Sprint 10: Testing (0/9 tasks)

**Total:** 0/63 tasks completed

---

## 🎯 Success Metrics

By completion:
- ✅ All activities functional
- ✅ 100% backend API integration
- ✅ Zero crashes
- ✅ Smooth animations
- ✅ Offline support
- ✅ Real-time updates
- ✅ Production-ready
- ✅ Play Store ready

---

**Ready to start? Let's begin with Sprint 1!**
