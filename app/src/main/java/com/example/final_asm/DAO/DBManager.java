package com.example.final_asm.DAO;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.final_asm.Model.Category;
import com.example.final_asm.Model.Expense;
import com.example.final_asm.Model.User;

import java.util.ArrayList;
import java.util.List;

public class DBManager extends SQLiteOpenHelper {


    private static final String DB_NAME = "expense_tracker.db";
    private static final int DB_VERSION = 5;

    // Table and column names
    public static final String USERS_TABLE = "user_data";
    public static final String USER_ID = "user_id";
    public static final String USERNAME = "username";
    public static final String USER_EMAIL = "email_address";
    public static final String USER_PASS = "password_hash";
    public static final String USER_REPEAT_PASS ="repeat_password_hash";
    public static final String USER_PHONE = "phone_number";
    public static final String USER_BUDGET = "budget_initial";

    public static final String CATEGORIES_TABLE = "category_data";
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_USER_ID = "user_category_id";
    public static final String CATEGORY_TITLE = "category_title";
    public static final String CATEGORY_BUDGET = "budget_allocated";

    public static final String EXPENSES_TABLE = "expense_records";
    public static final String EXPENSE_ID = "expense_id";
    public static final String EXPENSE_TITLE = "expense_title";
    public static final String EXPENSE_USER_ID = "user_expense_id";
    public static final String EXPENSE_COST = "expense_cost";
    public static final String EXPENSE_CATEGORY_ID = "expense_category_id";
    public static final String EXPENSE_DATE = "date_recorded";

    public static final String SESSION_TABLE = "user_session";
    public static final String SESSION_USER_ID = "session_user_id";
    public static final String SESSION_USERNAME = "session_username";
    public static final String SESSION_EMAIL = "session_email";
    public static final String SESSION_PHONE = "session_phone";
    public static final String SESSION_PASS = "session_pass";
    public static final String SESSION_REPEAT_PASS = "session_repeat_pass";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + USERS_TABLE + " (" +
                    USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    USERNAME + " TEXT, " +
                    USER_EMAIL + " TEXT UNIQUE, " +
                    USER_PHONE + " TEXT, " +
                    USER_PASS + " TEXT, " +
                    USER_REPEAT_PASS + " TEXT, " +
                    USER_BUDGET + " REAL DEFAULT 0" +
                    ");";

    private static final String CREATE_CATEGORIES_TABLE =
            "CREATE TABLE " + CATEGORIES_TABLE + " (" +
                    CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CATEGORY_USER_ID + " INTEGER, " +
                    CATEGORY_TITLE + " TEXT, " +
                    CATEGORY_BUDGET + " REAL, " +
                    "FOREIGN KEY(" + CATEGORY_USER_ID + ") REFERENCES " +
                    USERS_TABLE + "(" + USER_ID + ")" +
                    ");";

    private static final String CREATE_EXPENSES_TABLE =
            "CREATE TABLE " + EXPENSES_TABLE + " (" +
                    EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    EXPENSE_TITLE + " TEXT, " +
                    EXPENSE_USER_ID + " INTEGER, " +
                    EXPENSE_COST + " REAL, " +
                    EXPENSE_CATEGORY_ID + " INTEGER, " +
                    EXPENSE_DATE + " TEXT, " +
                    "FOREIGN KEY(" + EXPENSE_CATEGORY_ID + ") REFERENCES " +
                    CATEGORIES_TABLE + "(" + CATEGORY_ID + ")" +
                    ");";

    private static final String CREATE_SESSION_TABLE =
            "CREATE TABLE " + SESSION_TABLE + " (" +
                    SESSION_USER_ID + " INTEGER PRIMARY KEY, " +
                    SESSION_USERNAME + " TEXT, " +
                    SESSION_EMAIL + " TEXT, " +
                    SESSION_PHONE + " TEXT, " +
                    SESSION_PASS + " TEXT, " +
                    SESSION_REPEAT_PASS + " TEXT" +
                    ");";

    public DBManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_CATEGORIES_TABLE);
        db.execSQL(CREATE_EXPENSES_TABLE);
        db.execSQL(CREATE_SESSION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EXPENSES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SESSION_TABLE);
        onCreate(db);
    }

    // Session management methods
    public void storeSession(int userId, String name, String email, String phone, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SESSION_USER_ID, userId);
        values.put(SESSION_USERNAME, name);
        values.put(SESSION_EMAIL, email);
        values.put(SESSION_PHONE, phone);
        values.put(SESSION_PASS, password);
        db.replace(SESSION_TABLE, null, values);  // Use replace to update or insert new session
        db.close();
    }
    public Cursor retrieveSession() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(SESSION_TABLE, new String[]{SESSION_USER_ID, SESSION_USERNAME, SESSION_EMAIL, SESSION_PHONE, SESSION_PASS, SESSION_REPEAT_PASS},
                null, null, null, null, null);
    }

    public void eraseSession() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SESSION_TABLE, null, null);
        db.close();
    }

    public boolean checkActiveSession() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SESSION_TABLE, null, null, null, null, null, null);
        boolean sessionExists = cursor.moveToFirst();
        cursor.close();
        return sessionExists;
    }


    public User retrieveSessionUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SESSION_TABLE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex(SESSION_USER_ID));
            String name = cursor.getString(cursor.getColumnIndex(SESSION_USERNAME));
            String email = cursor.getString(cursor.getColumnIndex(SESSION_EMAIL));
            String phone = cursor.getString(cursor.getColumnIndex(SESSION_PHONE));
            String password = cursor.getString(cursor.getColumnIndex(SESSION_PASS));
            String repeatPassword = cursor.getString(cursor.getColumnIndex(SESSION_REPEAT_PASS));
            cursor.close();
            return new User(userId, name, email, phone, password, repeatPassword); // Added
        }
        cursor.close();
        return null;
    }

    // Category management methods
    public void createCategory(String name, double amount, int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CATEGORY_TITLE, name);
        values.put(CATEGORY_BUDGET, amount);
        values.put(CATEGORY_USER_ID, userId);
        db.insert(CATEGORIES_TABLE, null, values);
        db.close();
    }

    public void modifyCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CATEGORY_TITLE, category.getName());
        values.put(CATEGORY_BUDGET, category.getAmount());

        String whereClause = CATEGORY_ID + "=?";
        String[] whereArgs = {String.valueOf(category.getId())};

        db.update(CATEGORIES_TABLE, values, whereClause, whereArgs);
        db.close();
    }

    public void removeCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = CATEGORY_ID + "=?";
        String[] whereArgs = {String.valueOf(categoryId)};

        db.delete(CATEGORIES_TABLE, whereClause, whereArgs);
        db.close();
    }
    public String fetchCategoryNameById(int categoryId) {
        SQLiteDatabase db = getReadableDatabase();
        String categoryName = null;
        String query = "SELECT " + CATEGORY_TITLE + " FROM " + CATEGORIES_TABLE + " WHERE " + CATEGORY_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});

        if (cursor.moveToFirst()) {
            categoryName = cursor.getString(cursor.getColumnIndex(CATEGORY_TITLE));
        }
        cursor.close();
        db.close();
        return categoryName;
    }

    public List<Category> fetchCategories(int userId) {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(CATEGORIES_TABLE,
                new String[]{CATEGORY_ID, CATEGORY_TITLE, CATEGORY_BUDGET},
                CATEGORY_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(CATEGORY_ID));
                String name = cursor.getString(cursor.getColumnIndex(CATEGORY_TITLE));
                double amount = cursor.getDouble(cursor.getColumnIndex(CATEGORY_BUDGET));
                categories.add(new Category(id, name, amount));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    public double calculateTotalExpenses(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + EXPENSE_COST + ") FROM " + EXPENSES_TABLE + " WHERE " + EXPENSE_CATEGORY_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public boolean hasExpenses(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + EXPENSES_TABLE + " WHERE " + EXPENSE_CATEGORY_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});

        boolean hasExpenses = false;
        if (cursor.moveToFirst()) {
            hasExpenses = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return hasExpenses;
    }

    // Add repeatPassword handling in addUser method
    public void addUser(String username, String email, String phone, String password, String repeatPassword) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(USER_EMAIL, email);
        values.put(USER_PHONE, phone);
        values.put(USER_PASS, password);
        values.put(USER_REPEAT_PASS, repeatPassword);
        db.insert(USERS_TABLE, null, values);
        db.close();
    }

    //Su dung de login vao cai Login =))
    public Cursor authenticateUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE " + USERNAME + " = ? AND " + USER_PASS + " = ?";
        return db.rawQuery(query, new String[]{username, password});
    }

    public void modifyUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, user.getName());
        values.put(USER_EMAIL, user.getEmail());
        values.put(USER_PHONE, user.getPhone());
        values.put(USER_PASS, user.getPassword());
        values.put(USER_REPEAT_PASS, user.getRepeatPassword());


        String whereClause = USER_ID + "=?";
        String[] whereArgs = {String.valueOf(user.getId())};

        db.update(USERS_TABLE, values, whereClause, whereArgs);
        db.close();
    }

    // Expense management methods
    public void logExpense(String name, double cost, int categoryId, int userId, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EXPENSE_TITLE, name);
        values.put(EXPENSE_COST, cost);
        values.put(EXPENSE_CATEGORY_ID, categoryId);
        values.put(EXPENSE_USER_ID, userId);
        values.put(EXPENSE_DATE, date);
        db.insert(EXPENSES_TABLE, null, values);
        db.close();
    }

    public void removeExpense(int expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = EXPENSE_ID + "=?";
        String[] whereArgs = {String.valueOf(expenseId)};
        db.delete(EXPENSES_TABLE, whereClause, whereArgs);
        db.close();
    }

    public void modifyExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EXPENSE_TITLE, expense.getName());
        values.put(EXPENSE_COST, expense.getCost());
        values.put(EXPENSE_CATEGORY_ID, expense.getCategoryId());
        values.put(EXPENSE_USER_ID, expense.getUserId());
        values.put(EXPENSE_DATE, expense.getDate());

        String whereClause = EXPENSE_ID + "=?";
        String[] whereArgs = {String.valueOf(expense.getId())};

        db.update(EXPENSES_TABLE, values, whereClause, whereArgs);
        db.close();
    }

    public List<Expense> fetchExpenses(int categoryId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(EXPENSES_TABLE,
                new String[]{EXPENSE_ID, EXPENSE_TITLE, EXPENSE_COST, EXPENSE_DATE},
                EXPENSE_CATEGORY_ID + "=?",
                new String[]{String.valueOf(categoryId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(EXPENSE_ID));
                String name = cursor.getString(cursor.getColumnIndex(EXPENSE_TITLE));
                double cost = cursor.getDouble(cursor.getColumnIndex(EXPENSE_COST));
                String date = cursor.getString(cursor.getColumnIndex(EXPENSE_DATE));
                expenses.add(new Expense(id, name, cost, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }
    public double getInitialBudget(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"initial_budget"},
                "id = ?", new String[]{String.valueOf(userId)},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            double initialBudget = cursor.getDouble(cursor.getColumnIndex("initial_budget"));
            cursor.close();
            return initialBudget;
        }
        return 0.0;
    }

    public void updateInitialBudget(int userId, double initialBudget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("initial_budget", initialBudget);
        db.update("users", values, "id = ?", new String[]{String.valueOf(userId)});
    }



    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("phone", user.getPhone());
        values.put("password", user.getPassword());
        values.put("repeat password", user.getRepeatPassword());
        // Update the user record in the database
        String selection = "id = ?";
        String[] selectionArgs = { String.valueOf(user.getId()) };

        int count = db.update(
                "users",
                values,
                selection,
                selectionArgs
        );

        if (count > 0) {
            Log.d("DatabaseHelper", "User updated successfully.");
        } else {
            Log.d("DatabaseHelper", "User update failed.");
        }
    }
// DatabaseHelper.java

    public double computeTotalExpense(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + EXPENSE_COST + ") FROM " + EXPENSES_TABLE + " WHERE " + EXPENSE_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }
}