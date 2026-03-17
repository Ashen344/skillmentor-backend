
```
Frontend REPO - https://github.com/Ashen344/skillmentor-frontend/tree/skillmentor-frontend-final
Backend REPO - https://github.com/Ashen344/skillmentor-backend/tree/skillmentor-backend-final
Frontend deployment Link - https://skillmentor-frontend-tau.vercel.app
Backend deployment Link - https://skillmentor-backend-28l5.onrender.com

All the final changes are done in the brach
Frontend - skillmentor-frontend-final
Backend - skillmentor-backend-final
```

# Project - SkillMentor 

SkillMentor is a full-stack mentorship booking platform that connects students with professional mentors for 
personalised one-on-one learning sessions. Built for aspiring tech professionals, the platform focuses on high-demand certifications and career 
skills such as AWS Developer Associate and interview preparation.

# Implemented Features

### Admin
```
Create mentor profiles with full details (identity, professional info, stats)
Create subjects and assign them to a mentor
View all bookings in a searchable, filterable, sortable paginated table
Confirm payment, mark sessions as complete, or cancel any session
Add a meeting link to any session
```
### Student
```
Browse mentor listings on the home page without logging in
View full mentor profile page with bio, stats, certification badge, and subjects on personal mentor page
Book a session with a calendar picker — past dates are blocked
Subject pre-selection when clicking "Book This Subject" from a mentor profile
Double-booking protection — backend rejects overlapping sessions for both mentor and student
Personal dashboard showing all enrolled sessions with payment and session status
Write a star rating and review on completed sessions
```
### Platform
```
Clerk JWT authentication with role-based access control
Admin-only routes protected on both frontend and backend
Responsive design across mobile and desktop
```

## Tech Stack

### Frontend
```
React - UI franework
vite - Building tool for the dev server
shadcn - Styling tool
tailwind css - Styling tool
react-hook-form + Zod - State management and validation
```

### Backend
```
SpringBoot -Backend framework
Postgre SQL - Local db
Clerk JWKS - Sesssion verfication
```
### Deployment
```
Vercel - Frontend deployment 
Render - Backend deployment
Supabase - Cloud db
Clerk - Authentication 
```

## Local Deployment - Frontend
```
clone the git repo (git clone https://github.com/Ashen344/skillmentor-frontend.git)
open the cloned folder through VS code 
check the current branch (git branch)
switch to the final branch (git switch skillmentor-frontend-final)
install all the dependancies (npm install)
run (git checkout)
install the clerk configuration for react (npm install @clerk/react)
create the .env file and swap over the clerk public key
Frontend setup is done
```
## Local Deployment - Backend
```
clone the git repo (git clone https://github.com/Ashen344/skillmentor-backend.git)
open the cloned folder through intellij 
check the current branch (git branch)
switch to the final branch (git switch skillmentor-backend-final)
run (git checkout)
Backend setup is done
```

## Environmantal Variables - Frontend
```
VITE_API_BASE_UR
VITE_CLERK_PUBLISHABLE_KEY
```
## Environmantal Variables - Backend
```
CLERK_JWKS_URL
CORS_ALLOWED_ORIGINS
DATABASE_URL
DB_PASSWORD
DB_USERNAME
```
##API Documentation

https://skillmentor-backend-28l5.onrender.com/swagger-ui/index.html#/




## Project Structure

### Backend
```
skill-mentor-backend-service/
└── src/main/java/com/stemlink/skillmentor/
    ├── configs/          # SecurityConfig, CORS config, ModelMapper bean
    ├── constants/        # UserRoles constants (ADMIN, MENTOR, STUDENT)
    ├── controllers/      # REST controllers
    │   ├── MentorController.java
    │   ├── SubjectController.java
    │   ├── SessionController.java
    │   └── StudentController.java
    ├── dto/              # Request DTOs (MentorDTO, SubjectDTO, SessionDTO...)
    │   └── response/     # Response DTOs (MentorProfileDTO, AdminSessionResponseDTO)
    ├── entities/         # JPA entities (Mentor, Subject, Session, Student)
    ├── exceptions/       # SkillMentorException
    ├── respositories/    # Spring Data JPA repositories
    ├── security/         # ClerkValidator, UserPrincipal, AuthenticationFilter
    ├── services/         # Service interfaces
    │   └── impl/         # Service implementations
    └── utils/            # ValidationUtils (date, overlap checks)
```

### Frontend
```
skillmentor-frontend/src/
├── assets/                  # Logo and static assets
├── components/
│   ├── ui/                  # shadcn/ui base components (Button, Input, Card...)
│   ├── AdminLayout.tsx      # Admin sidebar and role guard
│   ├── MentorCard.tsx       # Mentor card displayed on home page
│   ├── Navigation.tsx       # Top navigation bar
│   └── SchedulingModel.tsx  # Booking modal with calendar + time picker
├── lib/
│   ├── api.ts               # All API call functions
│   └── utils.ts             # Tailwind cn() helper
├── pages/
│   ├── admin/
│   │   ├── AdminOverviewPage.tsx
│   │   ├── CreateMentorPage.tsx
│   │   ├── CreateSubjectPage.tsx
│   │   └── ManageBookingsPage.tsx
│   ├── DashboardPage.tsx    # Student session dashboard
│   ├── HomePage.tsx         # Public mentor listing
│   ├── MentorProfilePage.tsx
│   └── PaymentPage.tsx      # Session payment confirmation
├── types.ts                 # Shared TypeScript interfaces
└── App.tsx                  # Route definitions
```


