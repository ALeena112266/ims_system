package com.example.imssystem.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.imssystem.models.Complaint;
import com.example.imssystem.models.StatusLog;
import com.example.imssystem.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "grievance_db";
    private static final int DATABASE_VERSION = 4;

    // Table names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_COMPLAINTS = "complaints";
    private static final String TABLE_STATUS_LOGS = "status_logs";
    private static final String TABLE_TEMPLATES = "templates";

    // User table columns
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_ROLE = "role";
    private static final String KEY_USER_DEPARTMENT = "department";
    private static final String KEY_USER_STUDENT_ID = "student_id";
    private static final String KEY_USER_PASSWORD = "password";

    // Complaint table columns
    private static final String KEY_COMPLAINT_ID = "id";
    private static final String KEY_COMPLAINT_STUDENT_ID = "student_id";
    private static final String KEY_COMPLAINT_STUDENT_NAME = "student_name";
    private static final String KEY_COMPLAINT_CATEGORY = "category";
    private static final String KEY_COMPLAINT_DEPARTMENT = "department";
    private static final String KEY_COMPLAINT_TITLE = "title";
    private static final String KEY_COMPLAINT_DESCRIPTION = "description";
    private static final String KEY_COMPLAINT_PRIORITY = "priority";
    private static final String KEY_COMPLAINT_STATUS = "status";
    private static final String KEY_COMPLAINT_CREATED_AT = "created_at";
    private static final String KEY_COMPLAINT_UPDATED_AT = "updated_at";

    // Status log table columns
    private static final String KEY_LOG_ID = "id";
    private static final String KEY_LOG_COMPLAINT_ID = "complaint_id";
    private static final String KEY_LOG_UPDATED_BY = "updated_by";
    private static final String KEY_LOG_OLD_STATUS = "old_status";
    private static final String KEY_LOG_NEW_STATUS = "new_status";
    private static final String KEY_LOG_NOTE = "note";
    private static final String KEY_LOG_TIMESTAMP = "timestamp";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_NAME + " TEXT,"
                + KEY_USER_EMAIL + " TEXT UNIQUE,"
                + KEY_USER_ROLE + " TEXT,"
                + KEY_USER_DEPARTMENT + " TEXT,"
                + KEY_USER_STUDENT_ID + " TEXT,"
                + KEY_USER_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create complaints table
        String CREATE_COMPLAINTS_TABLE = "CREATE TABLE " + TABLE_COMPLAINTS + "("
                + KEY_COMPLAINT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_COMPLAINT_STUDENT_ID + " INTEGER,"
                + KEY_COMPLAINT_STUDENT_NAME + " TEXT,"
                + KEY_COMPLAINT_CATEGORY + " TEXT,"
                + KEY_COMPLAINT_DEPARTMENT + " TEXT,"
                + KEY_COMPLAINT_TITLE + " TEXT,"
                + KEY_COMPLAINT_DESCRIPTION + " TEXT,"
                + KEY_COMPLAINT_PRIORITY + " TEXT,"
                + KEY_COMPLAINT_STATUS + " TEXT,"
                + KEY_COMPLAINT_CREATED_AT + " TEXT,"
                + KEY_COMPLAINT_UPDATED_AT + " TEXT,"
                + "attachment_uri TEXT" + ")";
        db.execSQL(CREATE_COMPLAINTS_TABLE);

        // Create status logs table
        String CREATE_STATUS_LOGS_TABLE = "CREATE TABLE " + TABLE_STATUS_LOGS + "("
                + KEY_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_LOG_COMPLAINT_ID + " INTEGER,"
                + KEY_LOG_UPDATED_BY + " TEXT,"
                + KEY_LOG_OLD_STATUS + " TEXT,"
                + KEY_LOG_NEW_STATUS + " TEXT,"
                + KEY_LOG_NOTE + " TEXT,"
                + KEY_LOG_TIMESTAMP + " TEXT" + ")";
        db.execSQL(CREATE_STATUS_LOGS_TABLE);

        // Insert sample admin user
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, "Admin");
        values.put(KEY_USER_EMAIL, "admin@institute.edu");
        values.put(KEY_USER_ROLE, "admin");
        values.put(KEY_USER_DEPARTMENT, "Administration");
        values.put(KEY_USER_PASSWORD, "admin123");
        db.insert(TABLE_USERS, null, values);

        // Insert sample handler user
        ContentValues handlerValues = new ContentValues();
        handlerValues.put(KEY_USER_NAME, "IT Handler");
        handlerValues.put(KEY_USER_EMAIL, "ithandler@institute.edu");
        handlerValues.put(KEY_USER_ROLE, "handler");
        handlerValues.put(KEY_USER_DEPARTMENT, "IT Support");
        handlerValues.put(KEY_USER_PASSWORD, "handler123");
        db.insert(TABLE_USERS, null, handlerValues);

        // Create templates table
        String CREATE_TEMPLATES_TABLE = "CREATE TABLE " + TABLE_TEMPLATES + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "category TEXT,"
                + "department TEXT,"
                + "default_title TEXT,"
                + "default_description TEXT,"
                + "priority_hint TEXT" + ")";
        db.execSQL(CREATE_TEMPLATES_TABLE);

        // Insert sample templates
        insertTemplate(db, "Wi-Fi / Network issue", "IT", "IT Support",
                "Wi-Fi not working in Block C", "The Wi-Fi in Block C has been very slow and disconnecting frequently.", "High");
        insertTemplate(db, "Hostel cleanliness", "Hostel", "Hostel Admin",
                "Hostel room cleaning request", "My hostel room needs cleaning as it hasn't been attended to.", "Medium");
        insertTemplate(db, "Transport delay", "Transport", "Transport",
                "Morning bus delayed", "The morning bus was late today, causing me to miss my first class.", "Low");
        insertTemplate(db, "Fee query", "Accounts", "Accounts",
                "Fee payment issue", "I have a question about my recent fee payment receipt.", "Low");
    }

    private void insertTemplate(SQLiteDatabase db, String name, String category, String department, String defaultTitle, String defaultDescription, String priorityHint) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        values.put("department", department);
        values.put("default_title", defaultTitle);
        values.put("default_description", defaultDescription);
        values.put("priority_hint", priorityHint);
        db.insert(TABLE_TEMPLATES, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLAINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMPLATES);
        onCreate(db);
    }

    public List<com.example.imssystem.models.Template> getAllTemplates() {
        List<com.example.imssystem.models.Template> templateList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TEMPLATES, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                com.example.imssystem.models.Template template = new com.example.imssystem.models.Template();
                template.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))));
                template.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                template.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                template.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow("department")));
                template.setDefaultTitle(cursor.getString(cursor.getColumnIndexOrThrow("default_title")));
                template.setDefaultDescription(cursor.getString(cursor.getColumnIndexOrThrow("default_description")));
                template.setPriorityHint(cursor.getString(cursor.getColumnIndexOrThrow("priority_hint")));
                templateList.add(template);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return templateList;
    }

    // User methods
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, user.getName());
        values.put(KEY_USER_EMAIL, user.getEmail());
        values.put(KEY_USER_ROLE, user.getRole());
        values.put(KEY_USER_DEPARTMENT, user.getDepartment());
        values.put(KEY_USER_STUDENT_ID, user.getStudentId());
        values.put(KEY_USER_PASSWORD, user.getPassword());
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public User checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                KEY_USER_EMAIL + "=? AND " + KEY_USER_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID))));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)));
            user.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_DEPARTMENT)));
            user.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_STUDENT_ID)));
            cursor.close();
        }
        db.close();
        return user;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                KEY_USER_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Complaint methods
    public long addComplaint(Complaint complaint) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_COMPLAINT_STUDENT_ID, complaint.getStudentId());
        values.put(KEY_COMPLAINT_STUDENT_NAME, complaint.getStudentName());
        values.put(KEY_COMPLAINT_CATEGORY, complaint.getCategory());
        values.put(KEY_COMPLAINT_DEPARTMENT, complaint.getDepartment());
        values.put(KEY_COMPLAINT_TITLE, complaint.getTitle());
        values.put(KEY_COMPLAINT_DESCRIPTION, complaint.getDescription());
        values.put(KEY_COMPLAINT_PRIORITY, complaint.getPriority());
        values.put(KEY_COMPLAINT_STATUS, complaint.getStatus());
        values.put(KEY_COMPLAINT_CREATED_AT, getCurrentDateTime());
        values.put("attachment_uri", complaint.getAttachmentUri());
        long id = db.insert(TABLE_COMPLAINTS, null, values);
        db.close();
        return id;
    }

    public List<Complaint> getAllComplaintsByStudentId(String studentId) {
        List<Complaint> complaintList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMPLAINTS, null,
                KEY_COMPLAINT_STUDENT_ID + "=?",
                new String[]{studentId},
                null, null, KEY_COMPLAINT_CREATED_AT + " DESC");
        if (cursor.moveToFirst()) {
            do {
                Complaint complaint = new Complaint();
                complaint.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_ID))));
                complaint.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STUDENT_ID)));
                complaint.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STUDENT_NAME)));
                complaint.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_CATEGORY)));
                complaint.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_DEPARTMENT)));
                complaint.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_TITLE)));
                complaint.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_DESCRIPTION)));
                complaint.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_PRIORITY)));
                complaint.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STATUS)));
                complaint.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_CREATED_AT)));
                complaint.setAttachmentUri(cursor.getString(cursor.getColumnIndexOrThrow("attachment_uri")));
                complaintList.add(complaint);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return complaintList;
    }

    public Complaint getComplaintById(String complaintId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMPLAINTS, null,
                KEY_COMPLAINT_ID + "=?",
                new String[]{complaintId}, null, null, null);
        Complaint complaint = null;
        if (cursor != null && cursor.moveToFirst()) {
            complaint = new Complaint();
            complaint.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_ID))));
            complaint.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STUDENT_ID)));
            complaint.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STUDENT_NAME)));
            complaint.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_CATEGORY)));
            complaint.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_DEPARTMENT)));
            complaint.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_TITLE)));
            complaint.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_DESCRIPTION)));
            complaint.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_PRIORITY)));
            complaint.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STATUS)));
            complaint.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_CREATED_AT)));
            complaint.setAttachmentUri(cursor.getString(cursor.getColumnIndexOrThrow("attachment_uri")));
            cursor.close();
        }
        db.close();
        return complaint;
    }

    public int updateComplaintStatus(String complaintId, String newStatus, String updatedBy, String note) {
        Complaint complaint = getComplaintById(complaintId);
        if (complaint == null) return 0;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_COMPLAINT_STATUS, newStatus);
        values.put(KEY_COMPLAINT_UPDATED_AT, getCurrentDateTime());

        // Add status log
        addStatusLog(complaintId, updatedBy, complaint.getStatus(), newStatus, note);

        int rows = db.update(TABLE_COMPLAINTS, values,
                KEY_COMPLAINT_ID + "=?",
                new String[]{complaintId});
        db.close();
        return rows;
    }

    public List<Complaint> getAllComplaints() {
        List<Complaint> complaintList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMPLAINTS, null,
                null, null, null, null, KEY_COMPLAINT_CREATED_AT + " DESC");
        if (cursor.moveToFirst()) {
            do {
                Complaint complaint = new Complaint();
                complaint.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_ID))));
                complaint.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STUDENT_ID)));
                complaint.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STUDENT_NAME)));
                complaint.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_CATEGORY)));
                complaint.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_DEPARTMENT)));
                complaint.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_TITLE)));
                complaint.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_DESCRIPTION)));
                complaint.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_PRIORITY)));
                complaint.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STATUS)));
                complaint.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_CREATED_AT)));
                complaint.setAttachmentUri(cursor.getString(cursor.getColumnIndexOrThrow("attachment_uri")));
                complaintList.add(complaint);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return complaintList;
    }

    public List<Complaint> getComplaintsByDepartment(String department) {
        List<Complaint> complaintList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMPLAINTS, null,
                KEY_COMPLAINT_DEPARTMENT + "=?",
                new String[]{department},
                null, null, KEY_COMPLAINT_CREATED_AT + " DESC");
        if (cursor.moveToFirst()) {
            do {
                Complaint complaint = new Complaint();
                complaint.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_ID))));
                complaint.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STUDENT_ID)));
                complaint.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STUDENT_NAME)));
                complaint.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_CATEGORY)));
                complaint.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_DEPARTMENT)));
                complaint.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_TITLE)));
                complaint.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_DESCRIPTION)));
                complaint.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_PRIORITY)));
                complaint.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_STATUS)));
                complaint.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPLAINT_CREATED_AT)));
                complaint.setAttachmentUri(cursor.getString(cursor.getColumnIndexOrThrow("attachment_uri")));
                complaintList.add(complaint);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return complaintList;
    }

    // Status log methods
    public void addStatusLog(String complaintId, String updatedBy, String oldStatus, String newStatus, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LOG_COMPLAINT_ID, complaintId);
        values.put(KEY_LOG_UPDATED_BY, updatedBy);
        values.put(KEY_LOG_OLD_STATUS, oldStatus);
        values.put(KEY_LOG_NEW_STATUS, newStatus);
        values.put(KEY_LOG_NOTE, note);
        values.put(KEY_LOG_TIMESTAMP, getCurrentDateTime());
        db.insert(TABLE_STATUS_LOGS, null, values);
        db.close();
    }

    public List<StatusLog> getStatusLogsByComplaintId(String complaintId) {
        List<StatusLog> logList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STATUS_LOGS, null,
                KEY_LOG_COMPLAINT_ID + "=?",
                new String[]{complaintId},
                null, null, KEY_LOG_TIMESTAMP + " ASC");
        if (cursor.moveToFirst()) {
            do {
                StatusLog log = new StatusLog();
                log.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LOG_ID))));
                log.setComplaintId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOG_COMPLAINT_ID)));
                log.setUpdatedBy(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOG_UPDATED_BY)));
                log.setOldStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOG_OLD_STATUS)));
                log.setNewStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOG_NEW_STATUS)));
                log.setNote(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOG_NOTE)));
                log.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOG_TIMESTAMP)));
                logList.add(log);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return logList;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Dashboard stats
    public int getActiveComplaintsCount() {
        return getComplaintsByStatusCount("Under Review") + getComplaintsByStatusCount("Assigned") + getComplaintsByStatusCount("In Progress");
    }

    public int getResolvedComplaintsCount() {
        return getComplaintsByStatusCount("Resolved");
    }

    public int getEscalatedComplaintsCount() {
        return getComplaintsByStatusCount("Escalated");
    }

    public int getPendingComplaintsCount() {
        return getComplaintsByStatusCount("Submitted");
    }

    private int getComplaintsByStatusCount(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMPLAINTS, null,
                KEY_COMPLAINT_STATUS + "=?",
                new String[]{status}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public int deleteComplaint(String complaintId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_COMPLAINTS, KEY_COMPLAINT_ID + "=?",
                new String[]{complaintId});
        // Also delete status logs for this complaint
        db.delete(TABLE_STATUS_LOGS, KEY_LOG_COMPLAINT_ID + "=?",
                new String[]{complaintId});
        db.close();
        return rowsDeleted;
    }

    public User getUserById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                KEY_USER_ID + " = ?",
                new String[]{id},
                null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID))));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)));
            user.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_DEPARTMENT)));
            user.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_STUDENT_ID)));
            cursor.close();
        }
        db.close();
        return user;
    }

    public User getUserByStudentId(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                KEY_USER_STUDENT_ID + " = ?",
                new String[]{studentId},
                null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID))));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)));
            user.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_DEPARTMENT)));
            user.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_STUDENT_ID)));
            cursor.close();
        }
        db.close();
        return user;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID))));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)));
                user.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_DEPARTMENT)));
                user.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_STUDENT_ID)));
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }

    public int updateUserRole(String userId, String newRole) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ROLE, newRole);
        int rowsUpdated = db.update(TABLE_USERS, values, KEY_USER_ID + "=?",
                new String[]{userId});
        db.close();
        return rowsUpdated;
    }

    public int getComplaintsResolvedInPastWeek() {
        SQLiteDatabase db = this.getReadableDatabase();
        long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String oneWeekAgoStr = sdf.format(new Date(oneWeekAgo));

        Cursor cursor = db.query(TABLE_COMPLAINTS, null,
                KEY_COMPLAINT_STATUS + " IN ('Resolved', 'Closed') AND " + KEY_COMPLAINT_UPDATED_AT + " >= ?",
                new String[]{oneWeekAgoStr}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public int getComplaintsResolvedInPastMonth() {
        SQLiteDatabase db = this.getReadableDatabase();
        long oneMonthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String oneMonthAgoStr = sdf.format(new Date(oneMonthAgo));

        Cursor cursor = db.query(TABLE_COMPLAINTS, null,
                KEY_COMPLAINT_STATUS + " IN ('Resolved', 'Closed') AND " + KEY_COMPLAINT_UPDATED_AT + " >= ?",
                new String[]{oneMonthAgoStr}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public int getComplaintsResolvedInPastWeekByDepartment(String department) {
        SQLiteDatabase db = this.getReadableDatabase();
        long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String oneWeekAgoStr = sdf.format(new Date(oneWeekAgo));

        Cursor cursor = db.query(TABLE_COMPLAINTS, null,
                KEY_COMPLAINT_STATUS + " IN ('Resolved', 'Closed') AND " + KEY_COMPLAINT_DEPARTMENT + " = ? AND " + KEY_COMPLAINT_UPDATED_AT + " >= ?",
                new String[]{department, oneWeekAgoStr}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public int getComplaintsResolvedInPastMonthByDepartment(String department) {
        SQLiteDatabase db = this.getReadableDatabase();
        long oneMonthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String oneMonthAgoStr = sdf.format(new Date(oneMonthAgo));

        Cursor cursor = db.query(TABLE_COMPLAINTS, null,
                KEY_COMPLAINT_STATUS + " IN ('Resolved', 'Closed') AND " + KEY_COMPLAINT_DEPARTMENT + " = ? AND " + KEY_COMPLAINT_UPDATED_AT + " >= ?",
                new String[]{department, oneMonthAgoStr}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}
