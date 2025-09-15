# 📝 DailyJobFinder

**DailyJobFinder** is a Java-based application that collects IT job listings from Google Jobs using SerpAPI and sends a **daily email report** with job details and an optional image. 📧💼

---

## ✨ Features
- 🔍 Fetches IT-related job listings:
  - Java Developer, Backend Developer, Full Stack Developer, AI Engineer, Data Scientist, and more.
- ⏰ Sorts jobs by newest first.
- 📧 Sends an HTML-formatted email containing:
  - Job title
  - Company name
  - Source
  - Apply link
  - Date posted
- 🖼 Includes an optional image at the end of the email.
- 🔒 Fully configurable through environment variables (no hard-coded credentials).

---

## 🛠 Requirements
- Java 17+
- Maven
- Internet connection
- Gmail or SMTP-enabled email account for sending emails

---

## 🚀 Setup

### 1️⃣ Clone the repository
```bash
git clone https://github.com/YuvarajachariVadla/daily-job-finder.git
cd daily-job-finder
```
2️⃣ Set environment variables

Set these before running the application:

Windows (CMD)

```
setx SERPAPI_KEY "your_serpapi_key_here"
setx FROM_EMAIL "your_email@gmail.com"
setx SMTP_USER "your_email@gmail.com"
setx SMTP_PASS "your_smtp_app_password_here"
setx TO_EMAIL "recipient_email@gmail.com"
setx SMTP_HOST "smtp.gmail.com"  # optional, default
```
Linux / macOS (bash)

```
export SERPAPI_KEY="your_serpapi_key_here"
export FROM_EMAIL="your_email@gmail.com"
export SMTP_USER="your_email@gmail.com"
export SMTP_PASS="your_smtp_app_password_here"
export TO_EMAIL="recipient_email@gmail.com"
export SMTP_HOST="smtp.gmail.com"  # optional
```
⚠️ Gmail users must generate an App Password for SMTP authentication.

3️⃣ Ensure the email image exists

Place your image at:
```
src/main/resources/download.jpg
```

This image will appear at the end of the email. You can replace it with any image, but keep the file path the same. 🖼

4️⃣ Build and run the project
Using Maven
```
mvn clean compile
mvn exec:java -Dexec.mainClass="com.jobs.DailyJobFinder"
```
# Using IDE

Import the project into Eclipse, IntelliJ, or VS Code.

Ensure environment variables are set.

Run the DailyJobFinder class.

## 🛠 How it works

🔹 Fetch jobs: Uses SerpAPI to fetch job listings for predefined IT roles.

🔹 Process results: Sorts jobs by newest first and extracts title, company, source, link, and date.

🔹 Generate HTML: Builds an HTML-formatted email with tables and optional image.

🔹 Send email: Uses Jakarta Mail API to send an email via SMTP.

## 💼 Job Queries

The program searches for the following job titles:

Java Developer jobs

Backend Developer jobs

Full Stack Developer jobs

Software Engineer jobs

AI Engineer jobs

Business Analyst IT jobs

Data Analyst jobs

Data Scientist jobs

You can update these queries in the DailyJobFinder.java file under the IT_QUERIES list. 🔧

## ⚠️ Notes

Environment variables are required; the program will throw an error if any are missing. ❌

No hard-coded credentials are used for security. 🔒

Make sure your SMTP credentials allow sending emails (Gmail may require app passwords). ✅

If using a different email service, update SMTP_HOST and SMTP port in the code.

# 🤝 Contributing

* Fork the repository

* Make changes

* Submit a pull request
  
# 📜 License

This project is free to use for educational purposes. Please do not misuse credentials.

MIT License © Yuvaraj Vadla
