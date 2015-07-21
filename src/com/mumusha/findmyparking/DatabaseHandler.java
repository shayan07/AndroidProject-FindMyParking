package com.mumusha.findmyparking;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "archiveManger";

	// Contacts table name
	private static final String TABLE_CONTACTS = "archive";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_LA = "la";
	private static final String KEY_LO = "lo";
	private static final String KEY_NAME = "name";
	private static final String KEY_ADDR = "addr";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_LA + " TEXT," + KEY_LO
				+ " TEXT," + KEY_NAME + " TEXT," + KEY_ADDR + " TEXT" + ")";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

		// Create tables again
		onCreate(db);
	}

	// Adding new contact
	public void addArchive(archive contact) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_LA, contact.getLa());
		values.put(KEY_LO, contact.getLo());
		values.put(KEY_NAME, contact.getName());
		values.put(KEY_ADDR, contact.getAddr());
		db.insert(TABLE_CONTACTS, null, values);
		db.close(); // Closing database connection
	}

	// Getting single archive
	public archive getArchive(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID, KEY_LA,
				KEY_LO, KEY_NAME, KEY_ADDR }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		archive contact = new archive(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2), cursor.getString(3),
				cursor.getString(4));
		// return contact
		return contact;
	}

	// Getting All archives
	public ArrayList<archive> getAllArchive() {
		ArrayList<archive> contactList = new ArrayList<archive>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				archive contact = new archive();
				contact.setID(Integer.parseInt(cursor.getString(0)));
				contact.setLa(cursor.getString(1));
				contact.setLo(cursor.getString(2));
				contact.setName(cursor.getString(3));
				contact.setAddr(cursor.getString(4));
				// Adding contact to list
				contactList.add(contact);
			} while (cursor.moveToNext());
		}

		// return contact list
		return contactList;
	}

	// Getting contacts Count
	public int getArchiveCount() {
		String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		return cursor.getCount();
	}

	// Updating single contact
	public int updateArchive(archive contact) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_LA, contact.getLa());
		values.put(KEY_LO, contact.getLo());
		values.put(KEY_NAME, contact.getName());
		values.put(KEY_ADDR, contact.getAddr());
		// updating row
		return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
				new String[] { String.valueOf(contact.getID()) });
	}

	// Deleting single contact
	public void deleteArchive(archive contact) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
				new String[] { String.valueOf(contact.getID()) });
		db.close();
	}

}
