# Running CareerConnect Without a Laptop: Cloud & Standalone Guide

CareerConnect is architected to run **without a laptop running in the background**. You have two options depending on your presentation, demo, or evaluation scenario:

---

## Option 1: Standalone Mobile Mode (100% On-Device, Zero Laptop & Zero Server Needed)

If you only have your Android mobile phone and your laptop is completely turned off or unavailable:

1. **Install the APK directly on your Android phone**:
   - Transfer `apk/CareerConnect-debug.apk` to your phone via Google Drive, WhatsApp, Telegram, USB, or Bluetooth.
   - Tap the APK file on your Android phone to install (allow "Install from Unknown Sources" if prompted).
2. **Launch CareerConnect on your phone**:
   - On the **Login** or **Register** screen, tap the prominent button:
     $$\mathbf{\text{[ Run on Phone (No Laptop Required) ]}}$$
3. **What runs directly on your phone with zero server**:
   - **Local Session**: Automatically logs you in with candidate profile **Alex Chen** (B.Tech Computer Science, verified skills in Kotlin, Python, SQL, REST APIs, Git, Problem Solving).
   - **Real Jobs & Search**: Loads verified tech opportunities into local Android Room storage; filters, searches, and sorts with zero network delay.
   - **Exact 60/20/10/10 Matching Algorithm**: Computes live skill matching percentages, breakdown explanations, and compatibility ratings on the phone.
   - **Skill Gap Analysis**: Identifies acquired skills vs missing skills and calculates the exact score boost.
   - **Resume Upload & Parsing**: Allows uploading `.pdf` or `.docx` files, processes the text, and displays the user verification review screen.
   - **Saved Jobs & Application Tracking**: Bookmark jobs and move applications through `Interested`, `Applied`, `Interview`, `Rejected`, and `Offer`.
   - **In-App Notifications**: Full notification inbox with read/unread statuses and preference toggles.

---

## Option 2: Free 24/7 Cloud Hosting (FastAPI + PostgreSQL on the Internet)

If you want the full client-server backend running online 24/7 so anyone in the world can register their own account from their phone without your laptop:

### Method A: One-Click Deploy to Render.com (100% Free, No Credit Card Required)
Render provides free hosting for web services and free PostgreSQL databases:

1. Push your repository to **GitHub** (public or private).
2. Go to [Render.com](https://render.com) and sign up with GitHub.
3. Click **New +** $\rightarrow$ **Blueprint**.
4. Select your GitHub repository.
5. Render will detect `backend/render.yaml` and automatically spin up:
   - A free **PostgreSQL database** (`careerconnect-db`).
   - A free **FastAPI web service** running Uvicorn.
6. Once deployed, Render provides your public URL (e.g., `https://careerconnect-api.onrender.com`).
7. **Connect your Android phone to the Cloud**:
   - Open CareerConnect on your phone.
   - Go to **Settings** (gear icon in profile or bottom nav).
   - In **Backend Server Connection**, enter: `https://careerconnect-api.onrender.com/api/`
   - Tap **Save Server URL**.
   - Your Android phone is now communicating with the cloud backend 24/7 from mobile data (4G/5G) or Wi-Fi, completely independent of any laptop!

### Method B: Deploy to Railway.app or Koyeb
1. Connect your GitHub repository to Railway or Koyeb.
2. Select `backend/Dockerfile` or `backend/Procfile`.
3. Add a free PostgreSQL database extension.
4. Copy the public Railway/Koyeb domain into the Android app's **Settings** screen.

---

## Summary Comparison

| Feature | Standalone Mobile Mode | Free Cloud Deployment (Render/Railway) |
| :--- | :--- | :--- |
| **Laptop Required?** | ❌ **No (0% laptop)** | ❌ **No (0% laptop after deploy)** |
| **Internet Required?** | ❌ **Works 100% offline** | ✔️ Mobile data or Wi-Fi |
| **Backend Location** | On-device Room Database & local engine | Cloud servers running 24/7 |
| **All 22 Screens Working?** | ✔️ **Yes, fully operational** | ✔️ **Yes, fully operational** |
| **Best For** | Academic presentations, offline demos, viva | Multi-user evaluations, live cloud portfolio |
