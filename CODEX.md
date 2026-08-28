# JWT authentication implementation brief

## Application snapshot

SyncPaste is an online clipboard application that shares text (and intends to
share files) using a 4-digit OTP. The frontend is a Next.js 14 app under
`frontend/`; the backend is a Spring Boot 3.3.4 / Java 17 app under
`backend/`, using MySQL, Spring Data JPA/JDBC, Spring Security, and BCrypt.
The frontend development URL is `http://localhost:3000` and the backend runs
on `http://localhost:8081`.

The existing product promise is anonymous, OTP-based sharing: the page metadata
explicitly says that no sign-up is required. JWT authentication must therefore
add accounts and ownership without making OTP retrieval or anonymous sharing
require a login.

## Current frontend behavior

### API configuration

- `frontend/src/lib/constants.ts` exports `BACKEND_URL` as
  `http://localhost:8081`.
- `frontend/next.config.mjs` rewrites browser requests from `/api/:path*` to
  `http://localhost:8081/api/:path*` during development.
- The app uses a mix of relative `fetch('/api/...')` calls and direct Axios
  calls to `${BACKEND_URL}/api/...`. JWT handling needs one consistent API
  client.

### Existing account UI

- `frontend/src/app/register/page.tsx` sends
  `POST /api/users/register` with `{ username: email, password }`, then sends
  the user to `/login`.
- `frontend/src/app/login/page.tsx` sends `POST /api/users/login` with
  `{ username: email, password }`. On success it only writes the submitted
  email to `localStorage` under `username`; no credential is issued or stored.
- `frontend/src/app/components/Navbar.tsx` treats the presence of that
  `username` local-storage value as authentication. Logout posts to
  `POST /api/logout` and removes only `username`.
- `frontend/src/app/profile/page.tsx` reads the same `username` value and puts
  it in `PUT /api/users/profile`; this is user-controlled identity data rather
  than verified identity.
- `frontend/src/app/history/page.tsx` currently calls `GET /api/users` and
  renders all registered usernames/emails. This should be removed or replaced
  by an authenticated clipboard-history endpoint; a public user directory is a
  privacy issue.

### Clipboard / OTP flow

`frontend/src/app/components/streamline-clipboard.tsx` provides Share and
Receive tabs.

- The Share tab creates a client-side 4-digit OTP, accepts an expiration, and
  posts plaintext content to `POST /api/post/text` when encryption is off, or
  to `POST /api/encrypted/save` when it is on.
- The Receive tab currently always calls
  `GET /api/encrypted/retrieve/{otp}`.
- The file picker is UI-only: `selectedFile` is never uploaded and the backend
  has no multipart/file endpoint or file storage model.
- The UI sets an expiration, but the backend does not enforce it.

## Current backend behavior

### Data model

- `User` (`users` table): generated `id`, `username`, `password_hash`,
  `email`, unused `salt`, optional `name` and `gender`; transient `password`.
  There are no unique/non-null constraints in the entity for username/email.
- `ClipboardModel` (`clipboard_entries` table): generated `id`, optional
  `ManyToOne User user`, `deletedByUser`, `encryptedContent`,
  `encryptionKey`, unique 4-character `otp`, `expiryTime`, and `createdAt`.
  It can associate content with an account, but currently may remain
  anonymous.

### Existing endpoints

| Endpoint | Current behavior | JWT target |
| --- | --- | --- |
| `POST /api/users/register` | Creates a BCrypt password hash through raw JDBC; returns text. | Public. Validate input and return a safe response; optionally issue tokens after registration. |
| `POST /api/users/login` | Checks BCrypt password through raw JDBC; returns text. | Public. Return an access token and refresh-token mechanism/cookie. |
| `GET /api/users` | Returns all usernames/emails. | Remove or restrict to an admin-only feature; not needed for clipboard history. |
| `PUT /api/users/profile` | Uses `Principal`, but no authentication provider establishes one. | Authenticated; derive the target user from JWT, never request `username` in the body. |
| `POST /api/post/text` | Has `Principal`, saves clipboard and associates a user only when principal exists. | Public if anonymous sharing remains supported; JWT optionally links ownership. |
| `GET /api/get/text/{otp}` | Returns a clipboard entity as JSON, potentially exposing encryption key. | Public OTP retrieval only with a safe response DTO; do not expose encryption keys. |
| `POST /api/encrypted/save` | Encrypts server-side AES content and saves it. | Public optional-auth endpoint; if authenticated, associate the current user. |
| `GET /api/encrypted/retrieve/{otp}` | Retrieves/decrypts content. | Public OTP retrieval while valid; return appropriate 404/410 errors, never 500 for bad OTP. |
| `POST /api/logout` | Spring Security logout configuration; no server session is established. | For stateless access tokens, client clears its token; refresh-token logout should revoke/clear the refresh cookie. |

### Security configuration and gaps

`SecurityConfig` currently disables CSRF, permits register/login/post/retrieve
routes, and marks every other route authenticated. It has no
`UserDetailsService`, authentication manager, session policy, JWT filter, or
JWT signing/verification code. Consequently, a successful login does not
authenticate later requests and `Principal` is normally null.

The current CORS configuration permits `http://localhost:3000`, credentials,
and standard methods. Production needs an environment-provided frontend origin
(the README references `https://foryouclipboardapp.vercel.app/`), not a
hard-coded localhost value.

`application.properties` imports `.env` and reads database credentials from
environment properties. It currently uses `spring.jpa.hibernate.ddl-auto=create`,
which recreates schema and is unsafe for production. JWT secrets must never be
committed to this file or the frontend.

## Recommended JWT architecture

Use short-lived bearer access tokens and a rotating refresh token in a secure,
HTTP-only cookie.

- Access token: signed JWT (HS256 with a sufficiently random secret, or
  asymmetric signing if services will verify externally), lifetime about
  10–15 minutes. Claims: `sub` = immutable user ID, `username`, and token type
  (`access`). Do not place email, password data, or clipboard content in it.
- Refresh token: opaque random value stored hashed in a database table, rotated
  on every refresh; lifetime around 7–30 days. Send it in a `Secure`,
  `HttpOnly`, `SameSite` cookie scoped to `/api/auth`. This permits logout and
  revocation. If frontend and backend are different production sites, configure
  `SameSite=None; Secure`, exact CORS origin, and credentialed requests.
- Do not store long-lived JWTs in `localStorage`. The current username-only
  local storage is not proof of identity and must be replaced.
- Make the backend stateless:
  `SessionCreationPolicy.STATELESS`, a `OncePerRequestFilter` that parses
  `Authorization: Bearer <access token>`, validates signature/expiration/type,
  then places an authenticated principal in the SecurityContext.
- Add `UserDetailsService` backed by `UserRepository`, an
  `AuthenticationManager`/`DaoAuthenticationProvider`, and a `PasswordEncoder`
  bean (existing BCrypt can remain).
- Return DTOs, not JPA entities, from all public/auth routes. In particular,
  never serialize `passwordHash`, `salt`, `encryptionKey`, or lazy user data.

## Proposed backend work

1. Add a JWT library compatible with Spring Boot 3 (for example JJWT) and
   configuration properties such as `JWT_SECRET`, `JWT_ACCESS_TTL_SECONDS`,
   `REFRESH_TOKEN_TTL_DAYS`, and `APP_CORS_ALLOWED_ORIGIN`. Validate that the
   signing secret is present and strong at startup.
2. Add DTOs:
   `RegisterRequest`, `LoginRequest`, `AuthResponse`, `RefreshResponse`,
   `ProfileUpdateRequest`, and safe clipboard response/request DTOs. Registration
   should validate email, password length/strength, and duplicate username/email.
3. Make username and email unique at the database layer. Prefer one persistence
   style (JPA repositories) for user writes and reads instead of mixing raw JDBC
   and JPA. Add migrations (Flyway/Liquibase) before changing schemas and set
   production DDL handling to `validate` (or `none`).
4. Add a refresh-token table with user ID, hash, expiry, revocation/rotation
   metadata, and creation time. Store only a hash of the opaque token.
5. Add `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`, and
   `/api/auth/me`. Keep `/api/users/register` or migrate it to
   `/api/auth/register` consistently. Login must authenticate credentials and
   return user information plus an access token while setting the refresh cookie.
6. Update `SecurityConfig`: permit auth endpoints and OTP share/retrieve routes;
   require JWT for profile, clipboard history, delete/update, and any user list;
   add the JWT filter before `UsernamePasswordAuthenticationFilter`; configure
   JSON 401/403 responses.
7. Associate a clipboard with the authenticated user based only on the JWT
   principal. Public posts should retain `user = null`. Create
   `GET /api/clipboards/mine` (and delete/update endpoints if desired) for the
   signed-in owner's history.
8. Enforce `expiryTime` in both retrieval services. Do not let clients submit
   arbitrary values without server-side bounds. Return 410 Gone for an expired
   OTP and 404 for an unknown OTP. Use a cryptographically secure server-side OTP
   generator, retrying on collisions; four digits provides only 10,000 values, so
   rate-limit OTP attempts and consider six digits or a longer share code.
9. Resolve encryption semantics before representing it as end-to-end encryption:
   the server currently generates and stores the AES key alongside ciphertext,
   so it is server-side encryption at rest, not E2EE. Genuine E2EE requires
   client-side encryption and the server must not receive the decryption key.
10. Implement files separately: multipart upload/download endpoints, object or
    filesystem storage, file metadata (owner, OTP, content type, size, expiry),
    strict type/size limits, and expiry cleanup. Do not put files in JWTs or the
    current text field.

## Proposed frontend work

1. Create one API client that targets `BACKEND_URL` (or a
   `NEXT_PUBLIC_API_URL` environment variable). It should add the in-memory
   access token as an Authorization bearer header, request refresh with
   credentials, retry once after a 401, and handle logout on refresh failure.
2. Create an auth provider/context that obtains the current user from
   `/api/auth/me`, exposes login/register/logout, and displays loading state.
   Navbar, profile, and protected pages must use it instead of local storage.
3. On login, retain the access token in memory; the refresh cookie restores it
   after a full page load. Configure Axios/fetch with `withCredentials` /
   `credentials: 'include'` only when the refresh-cookie deployment model needs
   it.
4. Replace `/history` with the authenticated current user's clipboard history;
   do not display all users. Guard profile/history client-side for UX, while
   relying on the backend JWT check for real protection.
5. Keep the Share and Receive OTP actions usable while signed out. When signed
   in, the API client naturally attaches the bearer token so the backend records
   ownership. Handle invalid/expired OTP feedback in the UI.
6. Remove the direct use of `username` from profile update requests. Let the
   server choose the current user from JWT claims.

## Verification checklist

- Registration rejects duplicate identities and never returns password fields.
- Login returns a usable short-lived access token and sets a secure refresh
  cookie; invalid credentials return 401 without account-enumerating details.
- Authenticated `/api/auth/me`, profile update, and `/api/clipboards/mine`
  succeed only with a valid JWT; expired/tampered/missing tokens return 401.
- One user cannot modify or see another user's owned clipboard history.
- Anonymous users can still share and retrieve text by valid OTP; an expired OTP
  returns 410 and rate limiting resists brute-force attempts.
- Logout revokes/clears the refresh token and a subsequent refresh fails.
- Production CORS permits only the deployed frontend origin, supports the
  chosen cookie policy, and secrets are supplied through deployment environment
  variables.

## Important compatibility notes

- The frontend currently describes the login field as Email but transmits it as
  `username`. Decide whether email is the login identifier and implement that
  consistently; do not silently treat both as interchangeable without unique
  constraints and clear validation.
- `ClipboardModel` has the association required for per-user history, but its
  current public endpoints must not serialize it directly.
- Existing uncommitted backend changes were present during this review; this
  document is an assessment only and does not modify application source code.
