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
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "BookDB";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_BOOK_TABLE = "CREATE TABLE books ( "
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "box TEXT, "
				+ "envid TEXT, " + "person TEXT, " + "date TEXT, "
				+ "position TEXT, " + "wt TEXT" + ")";

		// create books table
		db.execSQL(CREATE_BOOK_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older books table if existed
		db.execSQL("DROP TABLE IF EXISTS books");

		// create fresh books table
		this.onCreate(db);
	}

	// Books table name
	private static final String TABLE_BOOKS = "books";

	// Books Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_BOX = "box";
	private static final String KEY_ENVID = "envid";
	private static final String KEY_PERSON = "person";
	private static final String KEY_DATE = "date";
	private static final String KEY_POSITION = "position";
	private static final String KEY_WT = "wt";

	private static final String[] COLUMNS = { KEY_ID, KEY_BOX, KEY_ENVID,
			KEY_PERSON, KEY_DATE, KEY_POSITION, KEY_WT };

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
		db.insert(TABLE_BOOKS, // table
				null, // nullColumnHack
				values); // key/value -> keys = column names/ values = column
							// values

		// 4. close
		db.close();
	}

	public InventoryRecord getBook(int id) {

		// 1. get reference to readable DB
		SQLiteDatabase db = this.getReadableDatabase();

		// 2. build query
		Cursor cursor = db.query(TABLE_BOOKS, // a. table
				COLUMNS, // b. column names
				" id = ?", // c. selections
				new String[] { String.valueOf(id) }, // d. selections args
				null, // e. group by
				null, // f. having
				null, // g. order by
				null); // h. limit

		// 3. if we got results get the first one
		if (cursor != null)
			cursor.moveToFirst();

		// 4. build book object
		InventoryRecord book = new InventoryRecord();

		book.setId(Integer.parseInt(cursor.getString(0)));
		book.setBox(cursor.getString(1));
		book.setEnvID(cursor.getString(2));
		book.setPersonID(cursor.getString(3));
		book.setDate(cursor.getString(4));
		book.setPosition(Integer.parseInt(cursor.getString(5)));
		book.setWt(cursor.getString(6));

		// log
		Log.d("getBook(" + id + ")", book.toString());

		// 5. return book
		return book;
	}

	public List<InventoryRecord> getAllBooks() {
		List<InventoryRecord> books = new LinkedList<InventoryRecord>();

		// 1. build the query
		String query = "SELECT  * FROM " + TABLE_BOOKS;

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

	public int updateBook(InventoryRecord book) {

		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();

		values.put("id", book.getId());
		values.put("box", book.getBox());
		values.put("envid", book.getEnvID());
		values.put("person", book.getPersonID());
		values.put("date", book.getDate());
		values.put("position", book.getPosition());
		values.put("wt", book.getWt());

		// 3. updating row
		int i = db.update(TABLE_BOOKS, // table
				values, // column/value
				KEY_ID + " = ?", // selections
				new String[] { String.valueOf(book.getId()) });
		db.close();

		return i;

	}

	public Boolean deleteBook(InventoryRecord book) {
		SQLiteDatabase db = this.getWritableDatabase();
		String num = "'" + Integer.toString(book.getPosition()) + "'";
		Log.d("deleteBook", book.toString());
		return db.delete(TABLE_BOOKS, KEY_POSITION + "=" + num, null) > 0;
	}

	public void deleteAllBooks() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_BOOKS, null, null);
	}

	public String[] getBoxList() {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.query(true, TABLE_BOOKS, new String[] { KEY_BOX },
				null, null, KEY_BOX, null, null, null);
		String[] boxes = new String[cursor.getCount()];
		ArrayList<String> arrcurval = new ArrayList<String>();
		if (cursor.moveToFirst()) {
			do {
				arrcurval.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}

		boxes = arrcurval.toArray(boxes);
		
		return boxes;
	}
}
