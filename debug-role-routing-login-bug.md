# Debug Session: role-routing-login-bug
Status: [OPEN]
Date: 2026-09-11
Symptoms:
- When logging in with handler email (ithandler@institute.edu) or admin email + the password saved in Firebase, user is NOT routed to HandlerDashboard/AdminDashboard respectively. Instead they end up on the wrong dashboard (likely Student Dashboard / DashboardActivity).
- Expected role Toast: "Logged in as: Handler (...)" or "Admin" — likely the role is resolved to student/empty at runtime.

## Hypotheses (Falsifiable)
H1: The Firestore `users` document for the handler/admin account has a `role` field that is NOT exactly `"handler"` or `"admin"` (e.g., uppercase `"Handler"`, typo `"hndler"`, extra whitespace, a different field name like `userRole` instead of `role`, or null/empty).
H2: The Firestore Auth UID does NOT match any Firestore document ID AND the fallback email lookup fails because the `email` field in the Firestore document is a different case/format/spaces than the typed login email (e.g., doc has `"ITHandler@Institute.edu"` but user types `"ithandler@institute.edu"`).
H3: The Firebase Auth sign-in succeeds but `getUserById(uid)` returns `null` → email fallback also fails → code creates a NEW default student user → routes to student dashboard. The original handler/admin Firestore doc is NEVER read.
H4: The `registerUser()` / `addUser()` flow was used to create the admin/handler and `userToMap()` correctly writes `role`, but the Firebase Console document was manually edited afterwards, setting role to a different value or removing the field. Alternatively `documentToUser()` falls back role to "student" when the field is any unexpected value.
H5: `HandlerDashboardActivity` / `AdminDashboardActivity` actually STARTS but immediately crashes or finishes during onCreate() due to a bug in their layout or code → the stack pops back to LoginActivity or Student Dashboard. The user sees this as "still can't go there".

## Evidence Log Plan
Instrument:
- FirebaseRepository.checkLogin() → report: auth UID, signIn success, getUserById result (null? role?), fallback email query: querySnapshot size, first doc role, NEW user created path.
- documentToUser() → report: raw role string, trimmed/lowercased role, fallback applied.
- LoginActivity routing decision → report: final role, which Intent class chosen (Admin/Handler/Dashboard), userId passed.
- HandlerDashboardActivity.onCreate & AdminDashboardActivity.onCreate → report: started, userId, userName, dept received, any exception in onCreate.

## Steps
1. ✅ Create debug session file + hypotheses
2. ⏳ Start Debug Server, instrument code with HTTP log reporting
3. ⏳ User runs app and reproduces (handler login, then admin login)
4. ⏳ Query /logs endpoint, analyze each hypothesis
5. ⏳ Apply minimal fix, re-run, compare pre vs post logs
6. ⏳ User confirms fix
7. ⏳ Clean up instrumentation + server
