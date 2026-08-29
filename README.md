# FractureCare

FractureCare is a Spring Boot and React application for protected X-ray uploads, AI-assisted fracture classification, optional Groq explanations, consented medical-professional reviews, prediction history, notifications, and downloadable PDF reports.

> **Important:** Results are AI-assisted decision support, not a diagnosis. Always seek qualified medical assessment for interpretation and care decisions.

## Included

- Account registration and login with BCrypt password hashing and time-limited JWT access tokens
- Repeated-login throttling and authenticated, owner-only access to predictions and reports
- JPEG/PNG content validation, image decoding checks, dimension limits, generated filenames, and private storage
- Three prototype outputs: no fracture, one fracture, and multiple fractures
- Confidence and risk presentation with a permanent clinical-review disclaimer
- Groq-powered plain-language result explanations with a safe rule-based fallback
- Medical professional accounts, consented reviews, and website notifications
- Paginated prediction history and server-generated PDF reports
- Flyway-managed MySQL schema and an H2 local demonstration profile
- Responsive React and TypeScript interface
- Docker definitions for the frontend, backend, and MySQL

## Technology

- Java 21 and Spring Boot 4.1.0
- Spring Web MVC, Spring Security, Spring Data JPA, Flyway, and Apache PDFBox
- MySQL 8.4 LTS for the normal runtime; H2 only for local demonstrations and tests
- React 19.2, TypeScript 5.9, Vite 8.2, and pnpm
- Separate Python/TensorFlow AI service in `ai-service/`, trained from the supplied FracAtlas dataset

## Quick start with Docker

Docker Desktop must be running.

```powershell
docker compose up --build
```

Open `http://localhost:5173`. The API health check is available at `http://localhost:8080/actuator/health`.

The Compose credentials are development-only. Replace every password and the JWT secret before deploying or sharing an environment.

## Local demonstration without MySQL

This mode stores local data in `backend/data/local` and uses the explicit mock analyser for development.

Backend:

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Frontend, in a second terminal:

```powershell
cd frontend
pnpm install
pnpm dev --host 127.0.0.1
```

Open `http://127.0.0.1:5173`.

## Run from IntelliJ IDEA and WebStorm

### IntelliJ IDEA — backend

1. Open the `backend` folder as a project and allow Maven to finish loading.
2. Select the shared **FractureCare Backend - Local** run configuration.
3. Confirm that its JDK is Java 21, then click Run.
4. Wait for `Tomcat started on port 8081` and `Started FractureCareApplication`.

The run configuration activates the `local` Spring profile. This uses the embedded H2 demonstration database on API port `8081`, so MySQL does not need to be running. If IntelliJ generated an older `FractureCareApplication` configuration, either use the shared configuration or open **Run > Edit Configurations** and set **Active profiles** to `local`.

### WebStorm — frontend

1. Open the `frontend` folder as a project.
2. Open `package.json` and run `pnpm install` once if dependencies are not present.
3. Click the Run icon beside the `dev` script, or create an npm run configuration using `frontend/package.json`, command `run`, script `dev`, and package manager `pnpm`.
4. Open `http://127.0.0.1:5173` after Vite reports that it is ready.

Start IntelliJ first, followed by WebStorm. Keep both applications running while using the prototype.

## Enable the Groq explanation assistant

The backend is already configured to use Groq when an API key is available. Initial prediction never contacts Groq. At the bottom of the result page, the user must explicitly choose **Ask Groq AI about this result**. Only then does the backend send the prediction class, system-defined category, confidence, and prediction model version. It never sends the X-ray, user name, email, or address.

1. Create an API key at `https://console.groq.com/keys`.
2. In IntelliJ, open **Run > Edit Configurations** and select the backend configuration you use.
3. Add an environment variable named `GROQ_API_KEY` with the API key as its value.
4. Restart the backend and create a new prediction.
5. At the bottom of the result, choose **Ask Groq AI about this result**.

The explanation card displays **Groq AI** when the remote explanation succeeds. It displays **Safety fallback** when no key is configured, Groq is unavailable, or its response is rejected. The fallback is intentional: a prediction must not fail because an explanatory service is offline.

The default model is `openai/gpt-oss-20b`, using Groq strict structured output. It can be changed with `GROQ_MODEL`. Set `EXPLANATION_MODE=rules` to disable external LLM requests. Enable Zero Data Retention in Groq's **Data Controls** before using anything beyond anonymous prototype data.

## Normal local development with MySQL

Create a MySQL database named `fracturecare`, then set the values shown in `.env.example` in your terminal or IDE. Start the backend without the `local` profile and start the frontend as shown above. Flyway creates the schema automatically.

For MySQL Workbench, open and execute `backend/database/setup-mysql.sql` using an administrator connection. In IntelliJ, select **FractureCare Backend - MySQL** and run it. The API uses port `8081`, and Flyway creates the `users`, `predictions`, and `reports` tables automatically. Refresh the **Schemas** panel in Workbench to see them under `fracturecare`.

## Verification

Backend tests:

```powershell
cd backend
mvn test
```

Frontend production build:

```powershell
cd frontend
pnpm build
```

The backend integration test verifies that initial prediction does not call the explanation layer and that explanation is generated only through the authenticated opt-in endpoint. It also covers registration, secured upload, history, PDF generation, and unauthenticated rejection. A separate local HTTP contract test verifies Groq request minimisation and structured-response parsing without contacting Groq during the test suite.

## Project layout

- `backend/` — Spring Boot API, security, persistence, storage, reporting, and tests
- `frontend/` — React application and production web-server configuration
- `ai-service/` — standalone PyCharm-friendly TensorFlow training and inference service
- `docs/AI_SERVICE_CONTRACT.md` — service boundary and Spring Boot handoff contract
- `compose.yaml` — local three-service environment

## AI service

The backend sends stored images to the standalone AI service when `AI_MODE=real` (the normal default). Open `ai-service/` in PyCharm, install `requirements-jupyter.txt`, and open `notebooks/FracAtlas_Model_Comparison.ipynb` in JupyterLab. The workflow trains Custom CNN, MobileNetV2 and EfficientNetB0 separately, selects using fracture-focused validation recall with macro F1 tie-breaking, and saves the selected model for `python -m app.main` on port `8090`.
