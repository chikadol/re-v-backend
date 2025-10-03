# RE:V — Multi-Module Workspace

- `:server-backend` — Spring Boot 3 + Kotlin + JPA (jakarta.persistence.*)
- `:android-app` — Android App + Room (androidx.room.*)

> 두 환경에서 ORM이 다릅니다. **백엔드에는 Room import가 없어야** 하고, **안드로이드에는 JPA import가 없어야** 합니다.

## 빌드/실행
### 서버 백엔드
```bash
cd server-backend
./gradlew bootRun
```
Swagger UI: http://localhost:8080/swagger-ui/index.html

### 안드로이드
Android Studio로 `:android-app` 모듈을 열고 빌드/실행하세요.
