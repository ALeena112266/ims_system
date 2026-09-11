package com.example.imssystem.helpers;

import android.content.Context;
import android.util.Log;

import com.example.imssystem.models.Complaint;
import com.example.imssystem.models.StatusLog;
import com.example.imssystem.models.Template;
import com.example.imssystem.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class FirebaseRepository {
    private static final String TAG = "FirebaseRepository";
   
    private static FirebaseRepository instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    


    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_COMPLAINTS = "complaints";
    private static final String COLLECTION_STATUS_LOGS = "status_logs";
    private static final String COLLECTION_TEMPLATES = "templates";

    public interface OnCompleteListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    private FirebaseRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseRepository();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void registerUser(User user, final OnCompleteListener<String> listener) {
        auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String uid = task.getResult().getUser().getUid();
                        user.setId(uid);
                        user.setPassword(null);
                        Map<String, Object> userData = userToMap(user);
                        db.collection(COLLECTION_USERS).document(uid).set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    seedTemplatesIfNeeded();
                                    listener.onSuccess(uid);
                                })
                                .addOnFailureListener(listener::onFailure);
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Registration failed"));
                    }
                });
    }

   public void checkLogin(String email, String password, final OnCompleteListener<User> listener) {

    String normalizedEmail = email.trim().toLowerCase();

    auth.signInWithEmailAndPassword(normalizedEmail, password)
            .addOnCompleteListener(task -> {

                if (task.isSuccessful() && task.getResult() != null) {

                    FirebaseUser firebaseUser = task.getResult().getUser();

                    if (firebaseUser == null) {
                        listener.onFailure(new Exception("Firebase user not found"));
                        return;
                    }

                    String uid = firebaseUser.getUid();

                    // Get user profile from Firestore using Firebase Authentication UID
                    getUserById(uid, new OnCompleteListener<User>() {

                        @Override
                        public void onSuccess(User user) {

                            if (user != null) {

                                // User exists in Firestore
                                // The role will be read from the Firestore document
                                listener.onSuccess(user);

                            } else {

                                // Do not create a default student account
                                listener.onFailure(
                                        new Exception(
                                                "User profile not found in Firestore. " +
                                                "Please contact the administrator."
                                        )
                                );
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            listener.onFailure(e);
                        }
                    });

                } else {

                    listener.onFailure(
                            task.getException() != null
                                    ? task.getException()
                                    : new Exception("Login failed")
                    );
                }
            });
}

                           
                
    

    public void isEmailExists(String email, final OnCompleteListener<Boolean> listener) {
        db.collection(COLLECTION_USERS).whereEqualTo("email", email).get()
                .addOnSuccessListener(querySnapshot -> listener.onSuccess(!querySnapshot.isEmpty()))
                .addOnFailureListener(listener::onFailure);
    }

    public void addUser(User user, final OnCompleteListener<String> listener) {
        auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String uid = task.getResult().getUser().getUid();
                        user.setId(uid);
                        user.setPassword(null);
                        Map<String, Object> userData = userToMap(user);
                        db.collection(COLLECTION_USERS).document(uid).set(userData)
                                .addOnSuccessListener(aVoid -> listener.onSuccess(uid))
                                .addOnFailureListener(listener::onFailure);
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Failed to add user"));
                    }
                });
    }

    public void getUserById(String id, final OnCompleteListener<User> listener) {
        db.collection(COLLECTION_USERS).document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentToUser(documentSnapshot);
                        listener.onSuccess(user);
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void getUserByStudentId(String studentId, final OnCompleteListener<User> listener) {
        db.collection(COLLECTION_USERS).whereEqualTo("studentId", studentId).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        listener.onSuccess(documentToUser(doc));
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void getAllUsers(final OnCompleteListener<List<User>> listener) {
        Log.d(TAG, "getAllUsers: querying Firestore users collection");
        db.collection(COLLECTION_USERS).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> userList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        userList.add(documentToUser(doc));
                    }
                    Log.d(TAG, "getAllUsers: SUCCESS — " + userList.size() + " users returned");
                    listener.onSuccess(userList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getAllUsers: FAILED — " + e.getMessage(), e);
                    listener.onFailure(e);
                });
    }

    public void updateUserRole(String userId, String newRole, final OnCompleteListener<Integer> listener) {
        db.collection(COLLECTION_USERS).document(userId).update("role", newRole)
                .addOnSuccessListener(aVoid -> listener.onSuccess(1))
                .addOnFailureListener(e -> listener.onSuccess(0));
    }

    public void addComplaint(Complaint complaint, final OnCompleteListener<String> listener) {
        complaint.setCreatedAt(getCurrentDateTime());
        if (complaint.getId() == null || complaint.getId().isEmpty()) {
            complaint.setId(db.collection(COLLECTION_COMPLAINTS).document().getId());
        }
        Map<String, Object> complaintData = complaintToMap(complaint);
        final String docId = complaint.getId();
        complaintData.put("id", docId);
        Log.d(TAG, "addComplaint: writing doc for studentId=" + complaint.getStudentId()
                + ", category=" + complaint.getCategory()
                + ", status=" + complaint.getStatus()
                + ", docId=" + docId);
        db.collection(COLLECTION_COMPLAINTS).document(docId).set(complaintData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "addComplaint: SUCCESS — Firestore doc ID = " + docId);
                    listener.onSuccess(docId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "addComplaint: FAILED — " + e.getMessage(), e);
                    listener.onFailure(e);
                });
    }

    public void getAllComplaintsByStudentId(String studentId, final OnCompleteListener<List<Complaint>> listener) {
        db.collection(COLLECTION_COMPLAINTS)
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Complaint> complaintList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        complaintList.add(documentToComplaint(doc));
                    }
                    sortComplaintsByCreatedAtDesc(complaintList);
                    listener.onSuccess(complaintList);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void getComplaintById(String complaintId, final OnCompleteListener<Complaint> listener) {
        db.collection(COLLECTION_COMPLAINTS).document(complaintId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onSuccess(documentToComplaint(documentSnapshot));
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void updateComplaintStatus(String complaintId, String newStatus, String updatedBy, String note, final OnCompleteListener<Integer> listener) {
        getComplaintById(complaintId, new OnCompleteListener<Complaint>() {
            @Override
            public void onSuccess(Complaint complaint) {
                if (complaint == null) {
                    listener.onSuccess(0);
                    return;
                }
                String oldStatus = complaint.getStatus();
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", newStatus);
                updates.put("updatedAt", getCurrentDateTime());

                db.collection(COLLECTION_COMPLAINTS).document(complaintId).update(updates)
                        .addOnSuccessListener(aVoid -> {
                            addStatusLog(complaintId, updatedBy, oldStatus, newStatus, note, new OnCompleteListener<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    listener.onSuccess(1);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    listener.onSuccess(1);
                                }
                            });
                        })
                        .addOnFailureListener(e -> listener.onSuccess(0));
            }

            @Override
            public void onFailure(Exception e) {
                listener.onSuccess(0);
            }
        });
    }

    public void getAllComplaints(final OnCompleteListener<List<Complaint>> listener) {
        db.collection(COLLECTION_COMPLAINTS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Complaint> complaintList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        complaintList.add(documentToComplaint(doc));
                    }
                    sortComplaintsByCreatedAtDesc(complaintList);
                    listener.onSuccess(complaintList);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void getComplaintsByDepartment(String department, final OnCompleteListener<List<Complaint>> listener) {
        db.collection(COLLECTION_COMPLAINTS)
                .whereEqualTo("department", department)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Complaint> complaintList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        complaintList.add(documentToComplaint(doc));
                    }
                    sortComplaintsByCreatedAtDesc(complaintList);
                    listener.onSuccess(complaintList);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void deleteComplaint(String complaintId, final OnCompleteListener<Integer> listener) {
        db.collection(COLLECTION_STATUS_LOGS).whereEqualTo("complaintId", complaintId).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                    db.collection(COLLECTION_COMPLAINTS).document(complaintId).delete()
                            .addOnSuccessListener(aVoid -> listener.onSuccess(1))
                            .addOnFailureListener(e -> listener.onSuccess(0));
                })
                .addOnFailureListener(e -> listener.onSuccess(0));
    }

    public void addStatusLog(String complaintId, String updatedBy, String oldStatus, String newStatus, String note, final OnCompleteListener<Void> listener) {
        StatusLog log = new StatusLog(complaintId, updatedBy, oldStatus, newStatus, note, getCurrentDateTime());
        Map<String, Object> logData = statusLogToMap(log);
        db.collection(COLLECTION_STATUS_LOGS).add(logData)
                .addOnSuccessListener(documentReference -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    public void getStatusLogsByComplaintId(String complaintId, final OnCompleteListener<List<StatusLog>> listener) {
        db.collection(COLLECTION_STATUS_LOGS)
                .whereEqualTo("complaintId", complaintId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<StatusLog> logList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        logList.add(documentToStatusLog(doc));
                    }
                    listener.onSuccess(logList);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void getAlertsForStudent(String studentId, final OnCompleteListener<List<StatusLog>> listener) {
        if (studentId == null || studentId.isEmpty()) {
            listener.onSuccess(Collections.emptyList());
            return;
        }
        getAllComplaintsByStudentId(studentId, new OnCompleteListener<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> myComplaints) {
                if (myComplaints == null || myComplaints.isEmpty()) {
                    listener.onSuccess(Collections.emptyList());
                    return;
                }
                final int total = myComplaints.size();
                final int[] remaining = {total};
                final CopyOnWriteArrayList<StatusLog> allLogs = new CopyOnWriteArrayList<>();
                final boolean[] anyFailed = {false};

                for (Complaint c : myComplaints) {
                    final String cid = c.getId();
                    if (cid == null || cid.isEmpty()) {
                        synchronized (remaining) {
                            remaining[0]--;
                            if (remaining[0] == 0) emitAll(listener, allLogs, anyFailed[0]);
                        }
                        continue;
                    }
                    getStatusLogsByComplaintId(cid, new OnCompleteListener<List<StatusLog>>() {
                        @Override
                        public void onSuccess(List<StatusLog> logsForCid) {
                            if (logsForCid != null) {
                                for (StatusLog sl : logsForCid) {
                                    if (sl.getNote() == null || sl.getNote().isEmpty()) {
                                        String title = c.getTitle();
                                        sl.setNote((title != null ? title : "Complaint")
                                                + " — changed from " + safe(sl.getOldStatus())
                                                + " to " + safe(sl.getNewStatus()));
                                    } else {
                                        String title = c.getTitle();
                                        sl.setNote((title != null ? title : "Complaint")
                                                + " — " + sl.getNote());
                                    }
                                }
                                allLogs.addAll(logsForCid);
                            }
                            synchronized (remaining) {
                                remaining[0]--;
                                if (remaining[0] == 0) emitAll(listener, allLogs, anyFailed[0]);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            anyFailed[0] = true;
                            synchronized (remaining) {
                                remaining[0]--;
                                if (remaining[0] == 0) emitAll(listener, allLogs, true);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    private static String safe(String s) { return s == null ? "—" : s; }

    private static void emitAll(OnCompleteListener<List<StatusLog>> listener,
                                CopyOnWriteArrayList<StatusLog> allLogs,
                                boolean anyFailed) {
        List<StatusLog> out = new ArrayList<>(allLogs);
        Collections.sort(out, (a, b) -> {
            String ta = a.getTimestamp();
            String tb = b.getTimestamp();
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });
        listener.onSuccess(out);
    }

    public void getAllTemplates(final OnCompleteListener<List<Template>> listener) {
        db.collection(COLLECTION_TEMPLATES).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Template> templateList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        templateList.add(documentToTemplate(doc));
                    }
                    if (templateList.isEmpty()) {
                        seedTemplatesIfNeeded();
                        listener.onSuccess(getDefaultTemplates());
                    } else {
                        listener.onSuccess(templateList);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onSuccess(getDefaultTemplates());
                });
    }

    public void getActiveComplaintsCount(final OnCompleteListener<Integer> listener) {
        String[] activeStatuses = {"Under Review", "Assigned", "In Progress"};
        int[] total = {0};
        final int[] remaining = {activeStatuses.length};

        for (String status : activeStatuses) {
            getComplaintsByStatusCount(status, new OnCompleteListener<Integer>() {
                @Override
                public void onSuccess(Integer count) {
                    total[0] += count;
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        listener.onSuccess(total[0]);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        listener.onSuccess(total[0]);
                    }
                }
            });
        }
    }

    public void getResolvedComplaintsCount(final OnCompleteListener<Integer> listener) {
        getComplaintsByStatusCount("Resolved", listener);
    }

    public void getEscalatedComplaintsCount(final OnCompleteListener<Integer> listener) {
        getComplaintsByStatusCount("Escalated", listener);
    }

    public void getPendingComplaintsCount(final OnCompleteListener<Integer> listener) {
        getComplaintsByStatusCount("Submitted", listener);
    }

    private void getComplaintsByStatusCount(String status, final OnCompleteListener<Integer> listener) {
        db.collection(COLLECTION_COMPLAINTS).whereEqualTo("status", status).get()
                .addOnSuccessListener(querySnapshot -> listener.onSuccess(querySnapshot.size()))
                .addOnFailureListener(e -> listener.onSuccess(0));
    }

    public void getComplaintsResolvedInPastWeek(final OnCompleteListener<Integer> listener) {
        final long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        getAllComplaints(new OnCompleteListener<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> all) {
                int count = 0;
                if (all != null) {
                    for (Complaint c : all) {
                        String status = c.getStatus();
                        if (!"Resolved".equals(status) && !"Closed".equals(status)) continue;
                        String ts = c.getUpdatedAt() != null ? c.getUpdatedAt() : c.getCreatedAt();
                        if (ts == null) continue;
                        try {
                            Date d = sdf.parse(ts);
                            if (d != null && d.getTime() >= oneWeekAgo) count++;
                        } catch (Exception ignore) { }
                    }
                }
                Log.d(TAG, "getComplaintsResolvedInPastWeek -> " + count);
                listener.onSuccess(count);
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "getComplaintsResolvedInPastWeek failed: " + e.getMessage(), e);
                listener.onSuccess(0);
            }
        });
    }

    public void getComplaintsResolvedInPastMonth(final OnCompleteListener<Integer> listener) {
        final long oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        getAllComplaints(new OnCompleteListener<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> all) {
                int count = 0;
                if (all != null) {
                    for (Complaint c : all) {
                        String status = c.getStatus();
                        if (!"Resolved".equals(status) && !"Closed".equals(status)) continue;
                        String ts = c.getUpdatedAt() != null ? c.getUpdatedAt() : c.getCreatedAt();
                        if (ts == null) continue;
                        try {
                            Date d = sdf.parse(ts);
                            if (d != null && d.getTime() >= oneMonthAgo) count++;
                        } catch (Exception ignore) { }
                    }
                }
                Log.d(TAG, "getComplaintsResolvedInPastMonth -> " + count);
                listener.onSuccess(count);
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "getComplaintsResolvedInPastMonth failed: " + e.getMessage(), e);
                listener.onSuccess(0);
            }
        });
    }

    public void getComplaintsResolvedInPastWeekByDepartment(final String department, final OnCompleteListener<Integer> listener) {
        final long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        getComplaintsByDepartment(department, new OnCompleteListener<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> list) {
                int count = 0;
                if (list != null) {
                    for (Complaint c : list) {
                        String status = c.getStatus();
                        if (!"Resolved".equals(status) && !"Closed".equals(status)) continue;
                        String ts = c.getUpdatedAt() != null ? c.getUpdatedAt() : c.getCreatedAt();
                        if (ts == null) continue;
                        try {
                            Date d = sdf.parse(ts);
                            if (d != null && d.getTime() >= oneWeekAgo) count++;
                        } catch (Exception ignore) { }
                    }
                }
                Log.d(TAG, "getComplaintsResolvedInPastWeekByDepartment(" + department + ") -> " + count);
                listener.onSuccess(count);
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "getComplaintsResolvedInPastWeekByDepartment(" + department + ") failed: " + e.getMessage(), e);
                listener.onSuccess(0);
            }
        });
    }

    public void getComplaintsResolvedInPastMonthByDepartment(final String department, final OnCompleteListener<Integer> listener) {
        final long oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        getComplaintsByDepartment(department, new OnCompleteListener<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> list) {
                int count = 0;
                if (list != null) {
                    for (Complaint c : list) {
                        String status = c.getStatus();
                        if (!"Resolved".equals(status) && !"Closed".equals(status)) continue;
                        String ts = c.getUpdatedAt() != null ? c.getUpdatedAt() : c.getCreatedAt();
                        if (ts == null) continue;
                        try {
                            Date d = sdf.parse(ts);
                            if (d != null && d.getTime() >= oneMonthAgo) count++;
                        } catch (Exception ignore) { }
                    }
                }
                Log.d(TAG, "getComplaintsResolvedInPastMonthByDepartment(" + department + ") -> " + count);
                listener.onSuccess(count);
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "getComplaintsResolvedInPastMonthByDepartment(" + department + ") failed: " + e.getMessage(), e);
                listener.onSuccess(0);
            }
        });
    }

    public void logout() {
        auth.signOut();
    }

    private void seedTemplatesIfNeeded() {
        db.collection(COLLECTION_TEMPLATES).limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        for (Template t : getDefaultTemplates()) {
                            Map<String, Object> data = templateToMap(t);
                            db.collection(COLLECTION_TEMPLATES).add(data);
                        }
                    }
                });
    }

    private List<Template> getDefaultTemplates() {
        List<Template> templates = new ArrayList<>();
        templates.add(new Template("Wi-Fi / Network issue", "IT", "IT Support",
                "Wi-Fi not working in Block C", "The Wi-Fi in Block C has been very slow and disconnecting frequently.", "High"));
        templates.add(new Template("Hostel cleanliness", "Hostel", "Hostel Admin",
                "Hostel room cleaning request", "My hostel room needs cleaning as it hasn't been attended to.", "Medium"));
        templates.add(new Template("Transport delay", "Transport", "Transport",
                "Morning bus delayed", "The morning bus was late today, causing me to miss my first class.", "Low"));
        templates.add(new Template("Fee query", "Accounts", "Accounts",
                "Fee payment issue", "I have a question about my recent fee payment receipt.", "Low"));
        return templates;
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("role", user.getRole());
        map.put("department", user.getDepartment());
        map.put("studentId", user.getStudentId());
        return map;
    }

    private User documentToUser(DocumentSnapshot doc) {
        User user = new User();
        user.setId(doc.getId());
        user.setName(doc.getString("name"));
        if (user.getName() == null) user.setName(doc.getString("Name"));
        user.setEmail(doc.getString("email"));
        if (user.getEmail() == null) user.setEmail(doc.getString("Email"));

        String rawRole = doc.getString("role");
        if (rawRole == null) rawRole = doc.getString("Role");
        if (rawRole == null) rawRole = doc.getString("userRole");
        if (rawRole == null) rawRole = doc.getString("UserRole");
        if (rawRole == null) rawRole = doc.getString("type");
        if (rawRole == null) rawRole = doc.getString("Type");

        if (rawRole != null) {
            rawRole = rawRole.trim().toLowerCase();
            if (rawRole.equals("administrator")) rawRole = "admin";
            if (rawRole.contains("manage") || rawRole.equals("moderator")) rawRole = "admin";
            if (rawRole.equals("staff") || rawRole.contains("officer") || rawRole.contains("worker")) rawRole = "handler";
        }
        if (rawRole == null || rawRole.isEmpty()) {
            rawRole = "student";
        }
        user.setRole(rawRole);

        String dept = doc.getString("department");
        if (dept == null) dept = doc.getString("Department");
        if (dept == null || dept.isEmpty()) {
            dept = "General";
        }
        user.setDepartment(dept.trim());

        String sid = doc.getString("studentId");
        if (sid == null) sid = doc.getString("StudentId");
        if (sid == null) sid = doc.getString("student_id");
        user.setStudentId(sid);
        return user;
    }

    private Map<String, Object> complaintToMap(Complaint c) {
        Map<String, Object> map = new HashMap<>();
        map.put("studentId", c.getStudentId());
        map.put("studentName", c.getStudentName());
        map.put("category", c.getCategory());
        map.put("department", c.getDepartment());
        map.put("title", c.getTitle());
        map.put("description", c.getDescription());
        map.put("priority", c.getPriority());
        map.put("status", c.getStatus());
        map.put("createdAt", c.getCreatedAt());
        map.put("updatedAt", c.getUpdatedAt());
        map.put("attachmentUri", c.getAttachmentUri());
        return map;
    }

    private Complaint documentToComplaint(DocumentSnapshot doc) {
        Complaint c = new Complaint();
        c.setId(doc.getId());
        c.setStudentId(doc.getString("studentId"));
        c.setStudentName(doc.getString("studentName"));
        c.setCategory(doc.getString("category"));
        c.setDepartment(doc.getString("department"));
        c.setTitle(doc.getString("title"));
        c.setDescription(doc.getString("description"));
        c.setPriority(doc.getString("priority"));
        c.setStatus(doc.getString("status"));
        c.setCreatedAt(doc.getString("createdAt"));
        c.setUpdatedAt(doc.getString("updatedAt"));
        c.setAttachmentUri(doc.getString("attachmentUri"));
        return c;
    }

    private Map<String, Object> statusLogToMap(StatusLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("complaintId", log.getComplaintId());
        map.put("updatedBy", log.getUpdatedBy());
        map.put("oldStatus", log.getOldStatus());
        map.put("newStatus", log.getNewStatus());
        map.put("note", log.getNote());
        map.put("timestamp", log.getTimestamp());
        return map;
    }

    private StatusLog documentToStatusLog(DocumentSnapshot doc) {
        StatusLog log = new StatusLog();
        log.setId(doc.getId());
        log.setComplaintId(doc.getString("complaintId"));
        log.setUpdatedBy(doc.getString("updatedBy"));
        log.setOldStatus(doc.getString("oldStatus"));
        log.setNewStatus(doc.getString("newStatus"));
        log.setNote(doc.getString("note"));
        log.setTimestamp(doc.getString("timestamp"));
        return log;
    }

    private Map<String, Object> templateToMap(Template t) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", t.getName());
        map.put("category", t.getCategory());
        map.put("department", t.getDepartment());
        map.put("defaultTitle", t.getDefaultTitle());
        map.put("defaultDescription", t.getDefaultDescription());
        map.put("priorityHint", t.getPriorityHint());
        return map;
    }

    private Template documentToTemplate(DocumentSnapshot doc) {
        Template t = new Template();
        t.setId(doc.getId());
        t.setName(doc.getString("name"));
        t.setCategory(doc.getString("category"));
        t.setDepartment(doc.getString("department"));
        t.setDefaultTitle(doc.getString("defaultTitle"));
        t.setDefaultDescription(doc.getString("defaultDescription"));
        t.setPriorityHint(doc.getString("priorityHint"));
        return t;
    }

    private void sortComplaintsByCreatedAtDesc(List<Complaint> list) {
        if (list == null || list.size() <= 1) return;
        Collections.sort(list, (c1, c2) -> {
            String a = c1.getCreatedAt();
            String b = c2.getCreatedAt();
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            return b.compareTo(a);
        });
    }
}
