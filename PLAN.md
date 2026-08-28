# JWT authentication for SyncPaste

  ## Summary

  Implement stateless JWT authentication using a short-lived
  access token and a rotating refresh token stored in an HTTP-
  only cookie. Preserve the existing anonymous OTP share-and-
  retrieve experience; authenticated sharing additionally
  records ownership for private history.

  Roles:

  - USER: default public-registration role; may manage only
    their own profile and clipboard history.

  - ADMIN: provisioned only from backend environment
    configuration; reserved for future operational moderation.
    It is never assignable through public registration.

  ## Backend changes

  - Add Role to User (USER default, ADMIN), make email and
    username unique, and use email as the canonical login
    identity. The registration UI will submit an email and
    password; backend stores the normalized email consistently
    for both existing username compatibility and email.

  - Introduce Flyway migrations for role, uniqueness
    constraints, and a refresh_tokens table. Refresh values are
    opaque random values stored only as hashes, with user
    ownership, expiry, revoked state, and rotation metadata.
    Change production schema handling from create to validate.

  - Add JWT configuration from environment variables: signing
    secret, access-token lifetime (15 minutes), refresh-token
    lifetime (14 days), CORS origin, and optional bootstrap-
    admin email. On startup, create/promote the configured
    bootstrap account to ADMIN; public registration always
    creates USER.

  - Add Spring Security JWT support: UserDetailsService,
    password authentication provider, stateless session policy,
    a OncePerRequestFilter that validates Authorization: Bearer
    <token>, and JSON 401/403 responses.

  - Replace text-only login responses with DTOs and implement:
      - POST /api/auth/register — creates a USER.
      - POST /api/auth/login — returns { accessToken, user } and
        sets the rotating refresh cookie.

      - POST /api/auth/refresh — rotates refresh token and
        returns a new access token.

      - POST /api/auth/logout — revokes refresh token and clears
        its cookie.

      - GET /api/auth/me — returns the authenticated user and
        role.

      - PUT /api/users/profile — uses JWT identity only; request
        contains editable profile fields, never username.

      - GET /api/clipboards/mine — returns only the signed-in
        user’s safe clipboard-history DTOs.

  - Keep OTP endpoints public. When a valid access token is
    supplied while creating a share, derive the owner from the
    JWT; otherwise store it anonymously. Enforce expiry server-
    side, generate OTPs server-side with SecureRandom, rate-
    limit OTP retrieval, return 404 for unknown and 410 for
    expired entries.

  - Remove or restrict GET /api/users; it must not expose
    registered users. Never serialize JPA User or ClipboardModel
    directly, particularly password hashes and encryption keys.

  - Treat existing “end-to-end encryption” as server-side
    encryption at rest until a later client-side encryption
    redesign. File upload/download remains a separate feature
    and is not part of JWT implementation.

  ## Frontend changes

  - Add a shared API client configured by NEXT_PUBLIC_API_URL.
    Keep access tokens in memory, attach them as bearer tokens,
    call refresh with credentials when needed, retry one failed
    authorized request, and force logout if refresh fails.

  - Add an auth provider that restores login state through /api/
    auth/me, exposes login/register/logout, and owns loading/
    error state.

  - Update login and registration to the new auth endpoints and
    email-based identity. Remove localStorage.username as an
    authentication signal.

  - Update Navbar, Profile, and History to use auth context.
    Profile sends only editable fields; History shows the
    signed-in user’s clipboard entries from /api/clipboards/
    mine, not all users.

  - Leave Share and Receive available to signed-out visitors.
    Signed-in shares automatically receive ownership through the
    bearer token. Display clear invalid/expired OTP errors.

  ## Test plan

  - Backend tests: registration validation and duplicate
    rejection; login, refresh rotation, logout revocation;
    tampered/expired/missing token rejection; USER ownership
    isolation; ADMIN bootstrap; anonymous and authenticated OTP
    sharing; expired/unknown OTP behavior; DTOs never leak
    passwords or encryption keys.

  - Frontend tests: login/logout state, protected profile/
    history UX, access-token refresh/retry behavior, and
    anonymous OTP sharing unchanged.

  - Integration checks: local CORS with refresh-cookie
    credentials, production-origin configuration, and a user’s
    history containing only their authenticated shares.

  ## Assumptions

  - Access tokens last 15 minutes; refresh tokens last 14 days
    and rotate on every refresh.

  - Public registration and OTP sharing remain available without
    sign-in.

  - Public registration creates only USER; ADMIN comes solely
    from APP_BOOTSTRAP_ADMIN_EMAIL.

  - Production uses HTTPS, exact configured frontend origin, and
    secure HTTP-only refresh cookies.

# Shareable Links for Text and
  Files

  ## Summary

  Add secure, reusable share links
  for every newly shared text or
  file. After successful sharing,
  display the link beneath the
  share form with:

  - Read-only share URL
  - Copy-to-clipboard button
  - Native device share button
    with copy fallback

  Friends can open the link
  without signing in and are taken
  to a dedicated /receive page
  that automatically loads the
  content.

  ## Key Changes

  ### Backend

  - Add a nullable, unique
    share_token field to
    ClipboardModel.

  - Generate a cryptographically
    secure, URL-safe token
    whenever text or file content
    is created.

  - Keep the existing 4-digit OTP
    for backward compatibility
    with manual receiving and
    history.

  - Add repository lookup support
    using shareToken.

  - Include shareToken in
    ClipboardResponse.

  - Update all creation flows—
    plain text, encrypted text,
    and files—to return the
    generated token consistently.

  - Add public endpoints:
      - GET /api/share/{token} —
        returns text content or
        file metadata.

      - GET /api/share/{token}/
        download — downloads a
        shared file.

  - Reuse the existing expiry
    checks. Links remain reusable
    until their configured
    expiration time.

  - Keep share endpoints publicly
    accessible through Spring
    Security, while returning
    clear 404/410 responses for
    invalid or expired links.

  - Preserve all existing OTP
    endpoints and behavior.

  ### Frontend

  - Add a dedicated /receive
    route.

  - Read the token from the URL
    query string, automatically
    fetch the shared content, and
    display:
      - Text directly in a read-
        only text area with a Copy
        button.

      - File name, size, type, and
        a Download button for
        files.

      - Clear invalid-link and
        expired-link states.

  - Update the share component to
    store the returned token and
    construct a frontend URL such
    as:

    https://your-frontend-domain.com/receive?token=<share-token>

  - Render the generated link
    below the Share button only
    after creation succeeds.

  - Add accessible Copy and Share
    icon buttons using the
    existing Lucide icon library.

  - Use navigator.share() when
    supported; otherwise copy the
    link and show confirmation.

  - Improve the existing receive
    input so pasted share URLs can
    be recognized and converted to
    either a token or OTP.

  - Keep the current OTP receive
    flow working unchanged.

  - Reset the generated link when
    starting a new share
    operation.

  ## Test Plan

  - Create and link-share plain
    text.

  - Create and link-share
    encrypted text.

  - Create and link-share an
    encrypted and unencrypted
    file.

  - Open links while logged out.
  - Confirm automatic loading on /
    receive.

  - Confirm text copying and file
    downloading.

  - Confirm native share behavior
    and clipboard fallback.

  - Confirm invalid, missing, and
    expired tokens show user-
    friendly errors.

  - Confirm links stop working
    after expiration.

  - Confirm existing OTP receiving
    still works.

  - Confirm generated tokens are
    unique and are not predictable
    4-digit OTPs.

  - Run backend Maven tests and
    the frontend production build.

  ## Assumptions

  - No social-specific share menu
    is required in this version.

  - Database schema updates may be
