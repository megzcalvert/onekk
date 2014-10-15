package org.wheatgenetics.onekk;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "onekkdb";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE sample (id INTEGER PRIMARY KEY AUTOINCREMENT, sample_id TEXT, photo TEXT, person TEXT, timestamp TEXT, weight TEXT)");
		db.execSQL("CREATE TABLE seed (id INTEGER PRIMARY KEY AUTOINCREMENT, sample_id TEXT, length TEXT, width TEXT, diameter TEXT, circularity TEXT, area TEXT, color TEXT )");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS sample");
		db.execSQL("DROP TABLE IF EXISTS seed");

		// create fresh tables
		this.onCreate(db);
	}

	// Table names
	private static final String TABLE_SAMPLE = "sample";
	private static final String TABLE_SEED = "seed";

	// Sample table columns names
	private static final String SAMPLE_ID = "id";
	private static final String SAMPLE_SID = "sample_id";
	private static final String SAMPLE_PHOTO = "photo";
	private static final String SAMPLE_PERSON = "person";
	private static final String SAMPLE_TIME = "timestamp";
	private static final String SAMPLE_WT = "weight";

	// Sample table columns names
	private static final String SEED_ID = "id";
	private static final String SEED_SID = "sample_id";
	private static final String SEED_LEN = "length";
	private static final String SEED_WID = "width";
	private static final String SEED_DIAM = "diameter";
	private static final String SEED_CIRC = "circularity";
	private static final String SEED_AREA = "area";
	private static final String SEED_COL = "color";

	// Sample table columns names
	private static final String KEY_ID = "id";
	private static final String KEY_BOX = "box";
	private static final String KEY_ENVID = "envid";
	private static final String KEY_PERSON = "person";
	private static final String KEY_DATE = "date";
	private static final String KEY_POSITION = "position";
	private static final String KEY_WT = "wt";

	private static final String[] SAMPLE_COLUMNS = { SAMPLE_ID, SAMPLE_SID,
			SAMPLE_PHOTO, SAMPLE_PERSON, SAMPLE_WT, SAMPLE_TIME };

	private static final String[] SEED_COLUMNS = { SEED_ID, SEED_SID, SEED_LEN,
			SEED_WID, SEED_DIAM, SEED_CIRC, SEED_AREA, SEED_COL };

	public void addSampleRecord(SampleRecord sample) {
		Log.d("Add Sample: ", sample.toString());

		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		
		values.put(SAMPLE_ID, sample.getBox());
		values.put(SAMPLE_SID, sample.getEnvID());
		values.put(SAMPLE_PHOTO, sample.getPersonID());
		values.put(SAMPLE_PERSON, sample.getDate());
		values.put(SAMPLE_TIME, sample.getWt());
		values.put(SAMPLE_WT, sample.getPosition());
		
		// 3. insert
		db.insert(TABLE_SAMPLE,null,values);

		// 4. close
		db.close();
	}
	
	public void addSeedRecord(SeedRecord seed) {
		// for logging
		Log.d("Add Seed: ", seed.toString());

		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(SEED_ID, seed.getBox());
		values.put(SEED_SID, seed.getEnvID());
		values.put(SEED_LEN, seed.getPersonID());
		values.put(SEED_WID, seed.getDate());
		values.put(SEED_DIAM, seed.getPosition());
		values.put(SEED_CIRC, seed.getWt());
		values.put(SEED_AREA, seed.getWt());
		values.put(SEED_COL, seed.getWt());
		
		// 3. insert
		db.insert(TABLE_SEED, null, values);

		// 4. close
		db.close();
	}

	public void addBook(InventoryRecord book) {
		// for logging
		Log.d("addBook", book.toString());

		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(KEY_BOX, book.getBox());
		values.put(KEY_ENVID, book.getEnvID());
		values.put(KEY_PERSON, book.getPersonID());
		values.put(KEY_DATE, book.getDate());
		values.put(KEY_POSITION, book.getPosition());
		values.put(KEY_WT, book.getWt());

		// 3. insert
		db.insert(TABLE_SAMPLE, // table
				null, // nullColumnHack
				values); // key/value -> keys = column names/ values = column
							// values

		// 4. close
		db.close();
	}

	//TODO query and return cursor
	public List<InventoryRecord> getAllBooks() {
		List<InventoryRecord> books = new LinkedList<InventoryRecord>();

		// 1. build the query
		String query = "SELECT  * FROM " + TABLE_SAMPLE;

		// 2. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		// 3. go over each row, build book and add it to list
		InventoryRecord book = null;
		if (cursor.moveToFirst()) {
			do {
				book = new InventoryRecord();
				book.setId(Integer.parseInt(cursor.getString(0)));
				book.setBox(cursor.getString(1));
				book.setEnvID(cursor.getString(2));
				book.setPersonID(cursor.getString(3));
				book.setDate(cursor.getString(4));
				book.setPosition(Integer.parseInt(cursor.getString(5)));
				book.setWt(cursor.getString(6));

				// Add book to books
				books.add(book);
			} while (cursor.moveToNext());
		}

		Log.d("getAllBooks()", books.toString());

		// return books
		return books;
	}

	public Boolean deleteSample(SampleRecord sample) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		String num = "'" + Integer.toString(sample.getPosition()) + "'";
		Log.d("Delete sample: ", sample.toString());
		
		//TODO delete from sample table and query seed table to delete
		return db.delete(TABLE_SAMPLE, KEY_POSITION + "=" + num, null) > 0;
	}

	public void deleteAll() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_SAMPLE, null, null);
		db.delete(TABLE_SEED, null, null);
	}
}
