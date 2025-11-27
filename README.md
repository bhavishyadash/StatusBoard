# StatusBoard 📱  
*A smart, real-time status sharing Android app.*

StatusBoard is a mobile app that lets users set their availability (“Free”, “Do Not Disturb”, “Away”, “Sleeping”) and (eventually) share it with friends. Upcoming features include Google Calendar auto-status, real-time syncing, and a friend system powered by Firebase.

This project is my personal challenge during Fall Break — built from scratch to learn Android, real-time systems, backend integration, and clean UI.

---

## 🚀 Current Features

- Clean single-screen UI
- Four core status modes:
  - 🟢 **Free** – available to chat & take calls  
  - ⛔ **Do Not Disturb** – busy, might reply later  
  - 🟡 **Away** – not near devices / out & about  
  - 😴 **Sleeping** – basically offline  
- Status selection handled via a `UserStatus` enum in Java

---

## 🎯 Planned Features

- 🔐 Firebase Authentication (Google sign-in)
- ☁️ Firestore backend for storing users & statuses
- 👥 Friend system (add/accept friends, view their statuses)
- 🔄 Real-time status updates using Firestore listeners
- 📅 Google Calendar integration (auto DND during events)
- 🌓 Smart status scheduling (e.g., auto “Sleeping” at night)
- ⚙️ Privacy controls (who can see your status)
- 🎨 More polished UI (Material 3, theming, animations)

---

## 🛠 Tech Stack

| Layer      | Tech                              |
|-----------|------------------------------------|
| Language  | Java                              |
| Platform  | Android (Android Studio)          |
| UI        | XML layouts, Material components  |
| Backend   | (Planned) Firebase Auth, Firestore |
| Integrations | (Planned) Google Calendar API  |

---

## 📂 Project Structure

```text
StatusBoard/
│
├── app/
│   ├── java/com.example.statusboard/
│   │   ├── MainActivity.java
│   │   └── UserStatus.java
│   │   // future: auth/, firestore/, calendar/, ui/ packages
│   │
│   └── res/
│       ├── layout/activity_main.xml
│       ├── drawable/
│       └── values/
│
└── README.md
