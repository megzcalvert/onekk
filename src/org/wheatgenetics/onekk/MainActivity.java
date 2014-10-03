package org.wheatgenetics.onekk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener {

	public final static String TAG = "ScaleActivity";
	public final static String PREFS = "PREFS";
	private SharedPreferences mSettings;
	private UsbDevice mDevice;
	private double mWeightGrams = 0;
	private double mZeroGrams = 0;
	private String mUnitsText = "grams";
	private String boxNumber;
	private String personID;
	private TextView mUnitsView;
	private EditText mWeightEditText;
	private TextView boxNumTextView;
	TextView boxNumTV;
	TextView itemNumTV;
	TextView envIDTV;
	TextView weightTV;
	TextView boxHeader;
	TextView itemHeader;
	TextView idHeader;
	TextView weightHeader;
	private Button setBox;
	private EditText inputText;
	TableLayout InventoryTable;
	MySQLiteHelper db;
	String firstName = "";
	String lastName = "";
	List<InventoryRecord> list;
	int itemCount;
	ScrollView sv1;
	static int currentItemNum = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scale);
		Log.v(TAG, "onCreate");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Define UI elements
		mSettings = getSharedPreferences(PREFS, 0);
		sv1 = (ScrollView) findViewById(R.id.svData);
		mUnitsText = mSettings.getString("unitsText", "grams");
		mUnitsView = (TextView) findViewById(R.id.text_unit);
		mUnitsView.setText(mUnitsText);
		mWeightEditText = (EditText) findViewById(R.id.text_weight);
		mWeightEditText.setText("Not connected");
		boxNumTextView = (TextView) findViewById(R.id.tvBoxNum);
		boxNumTextView.setText("");
		setBox = (Button) findViewById(R.id.btBox);
		inputText = (EditText) findViewById(R.id.etInput);
		boxHeader = (TextView) findViewById(R.id.tvBoxTable);
		itemHeader = (TextView) findViewById(R.id.tvNumTable);
		idHeader = (TextView) findViewById(R.id.tvIdTable);
		weightHeader = (TextView) findViewById(R.id.tvWtTable);
		InventoryTable = (TableLayout) findViewById(R.id.tlInventory);
		db = new MySQLiteHelper(this);

		setBox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setBoxDialog();
			}
		});

		InventoryTable.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});

		Intent intent = getIntent();
		mDevice = (UsbDevice) intent
				.getParcelableExtra(UsbManager.EXTRA_DEVICE);

		inputText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					mgr.showSoftInput(inputText,
							InputMethodManager.HIDE_IMPLICIT_ONLY);
					if (event.getAction() != KeyEvent.ACTION_DOWN)
						return true;
					addRecord(); // Add the current record to the table
					goToBottom();
					inputText.requestFocus(); // Set focus back to Enter box
				}
				
				if (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						return true;
					}
					if (event.getAction() == KeyEvent.ACTION_UP) {
						addRecord(); // Add the current record to the table
						goToBottom();
					}
					inputText.requestFocus(); // Set focus back to Enter box
				}
				return false;
			}
		});

		mWeightEditText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
					InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					mgr.showSoftInput(inputText,
							InputMethodManager.HIDE_IMPLICIT_ONLY);
					if (event.getAction() != KeyEvent.ACTION_DOWN)
						return true;
					addRecord(); // Add the current record to the table
					goToBottom();
					
					if(mDevice != null) {
					mWeightEditText.setText("");
					}
					inputText.requestFocus(); // Set focus back to Enter box
				}
				
				if (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						return true;
					}
					if (event.getAction() == KeyEvent.ACTION_UP) {
						addRecord(); // Add the current record to the table
						goToBottom();
					}
					inputText.requestFocus(); // Set focus back to Enter box
				}
				return false;
			}
		});
		
		setPersonDialog();
		findScale();
		createDirectory();
		parseDbToTable();
		goToBottom();
	}

	private void goToBottom() {
		sv1.post(new Runnable() {
			public void run() {
				sv1.fullScroll(ScrollView.FOCUS_DOWN);
				inputText.requestFocus();
			}
		});
	}

	private void parseDbToTable() {
		InventoryTable.removeAllViews();
		list = db.getAllBooks();
		itemCount = list.size();
		if (itemCount != 0) {
			for (int i = 0; i < itemCount; i++) {
				String[] temp = list.get(i).toString().split(",");
				Log.e(TAG, temp[0] + " " + Integer.parseInt(temp[4]) + " "
						+ temp[1] + " " + temp[5]);
				createNewTableEntry(temp[0], Integer.parseInt(temp[4]),
						temp[1], temp[5]);
				currentItemNum = Integer.parseInt(temp[4]) + 1;
			}
		}
	}

	/**
	 * Adds a new record to the internal list of records
	 */
	private void addRecord() {
		String ut;
		String date = getDate();

		ut = inputText.getText().toString();
		if (ut.equals("")) {
			return; // check for empty user input
		}

		String weight;
		if (mDevice == null && mWeightEditText.getText().toString().equals("Not connected")) {
			weight = "null";
		} else {
			weight = mWeightEditText.getText().toString();
		}

		db.addBook(new InventoryRecord(boxNumTextView.getText().toString(), inputText
				.getText().toString(), personID, date, currentItemNum, weight)); // add
																					// to
																					// database

		createNewTableEntry(boxNumTextView.getText().toString(),
				currentItemNum, inputText.getText().toString(), weight);
		currentItemNum++;
	}

	private String getDate() {
		String date = "";
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		date = Integer.toString(year) + "-" + Integer.toString(month) + "-"
				+ Integer.toString(day);
		return date;
	}

	/**
	 * Adds a new entry to the end of the TableView
	 * 
	 * @param bn
	 *            - Box ID
	 * @param in
	 *            - Position
	 * @param en
	 *            - Sample ID
	 * @param wt
	 *            - Sample weight
	 */
	private void createNewTableEntry(String bn, int in, String en, String wt) {
		String tag = bn + "," + en + "," + in;
		inputText.setText("");
		
		
		/* Create a new row to be added. */
		TableRow tr = new TableRow(this);
		tr.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		/* Create the item number field */
		itemNumTV = new TextView(this);
		itemNumTV.setGravity(Gravity.CENTER | Gravity.BOTTOM);
		itemNumTV.setTextColor(Color.BLACK);
		itemNumTV.setTextSize(20.0f);
		itemNumTV.setText("" + in);
		itemNumTV.setLayoutParams(new TableRow.LayoutParams(0,
				LayoutParams.WRAP_CONTENT, 0.16f));

		/* Create the box number field */
		boxNumTV = new TextView(this);
		boxNumTV.setGravity(Gravity.CENTER | Gravity.BOTTOM);
		boxNumTV.setTextColor(Color.BLACK);
		boxNumTV.setTextSize(20.0f);
		boxNumTV.setText(bn);
		boxNumTV.setLayoutParams(new TableRow.LayoutParams(0,
				LayoutParams.WRAP_CONTENT, 0.16f));

		/* Create the Envelope ID field */
		envIDTV = new TextView(this);
		envIDTV.setGravity(Gravity.CENTER | Gravity.BOTTOM);
		envIDTV.setTextColor(Color.BLACK);
		envIDTV.setTextSize(20.0f);
		envIDTV.setText(en);
		envIDTV.setTag(tag);
		envIDTV.setLayoutParams(new TableRow.LayoutParams(0,
				LayoutParams.WRAP_CONTENT, 0.5f));
		envIDTV.setLongClickable(true);

		/* Define the listener for the longclick event */
		envIDTV.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				final String tag = (String) v.getTag();
				deleteDialog(tag);
				return false;
			}
		});

		/* Create the Weight field */
		weightTV = new TextView(this);
		weightTV.setGravity(Gravity.CENTER | Gravity.BOTTOM);
		weightTV.setTextColor(Color.BLACK);
		weightTV.setTextSize(20.0f);
		weightTV.setText(wt);
		weightTV.setLayoutParams(new TableRow.LayoutParams(0,
				LayoutParams.WRAP_CONTENT, 0.16f));

		/* Add UI elements to row and add row to table */
		tr.addView(itemNumTV);
		tr.addView(boxNumTV);
		tr.addView(envIDTV);
		tr.addView(weightTV);
		InventoryTable.addView(tr, new LayoutParams(
				TableLayout.LayoutParams.MATCH_PARENT,
				TableLayout.LayoutParams.MATCH_PARENT));
	}

	private void deleteDialog(String tag) {
		final String tagArray[] = tag.split(",");
		final String fBox = tagArray[0];
		final String fEnv = tagArray[1];
		final int fNum = Integer.parseInt(tagArray[2]);

		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this);
		builder.setTitle("Delete Entry");
		builder.setMessage("Delete " + fEnv + "?")
				.setCancelable(true)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								InventoryRecord temp = new InventoryRecord(fBox, fEnv, null, null,
										fNum, null);
								db.deleteBook(temp);
								parseDbToTable();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void createDirectory() {
		try {
			File myFile = new File("/sdcard/Inventory/");
			if (!myFile.exists()) {
				if (!myFile.mkdirs()) {
					Toast.makeText(getBaseContext(),
							"Problem making directory", Toast.LENGTH_SHORT)
							.show();
				}
			}
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void setBoxDialog() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		input.setText(boxNumber);
		input.selectAll();
		input.setSingleLine();
		alert.setTitle("Set Box");
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString().trim();
				boxNumTextView.setText(value);

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
					}
				});
		AlertDialog alertD = alert.create();
		alertD.show();
		alertD.getWindow().setLayout(600, 400);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.v(TAG, "onStart");

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.scale_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scaleConnect:
			findScale();
			break;
		case R.id.person:
			setPersonDialog();
			break;
		case R.id.export:
			export();
			break;
		case R.id.clearData:
			clearDialog();
			break;
		case R.id.help:
			helpDialog();
			break;
		case R.id.about:
			aboutDialog();
			break;
		}

		Log.i(TAG, String.format("item selected %s", item.getTitle()));
		return true;
	}

	private void helpDialog() {
	}

	private void aboutDialog() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		LayoutInflater inflater = this.getLayoutInflater();
		final View personView = inflater.inflate(R.layout.about, null);

		alert.setCancelable(true);
		alert.setTitle("About Inventory");
		alert.setView(personView);
		alert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}
		});
		alert.show();
	}

	public void makeToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void setPersonDialog() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View personView = inflater.inflate(R.layout.person, null);
		final EditText fName = (EditText) personView
				.findViewById(R.id.firstName);
		final EditText lName = (EditText) personView
				.findViewById(R.id.lastName);
		fName.setText(firstName);
		lName.setText(lastName);

		alert.setCancelable(false);
		alert.setTitle("Set Person");
		alert.setView(personView);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				firstName = fName.getText().toString().trim();
				lastName = lName.getText().toString().trim();

				if (firstName.length() == 0 | lastName.length() == 0) {
					makeToast("Names cannot be left blank.");
					setPersonDialog();
					return;
				}

				String input = firstName + "_" + lastName;
				makeToast("Person set as: " + input);
				input = input.replace(" ", "_");
				personID = input.toLowerCase();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(lName.getWindowToken(), 0);
			}
		});
		alert.show();
	}

	private void clearDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this);
		builder.setMessage(
				"This will delete all data from the database. Proceed?")
				.setCancelable(false)
				.setTitle("Clear Data")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								boxNumTextView.setText("");
								makeToast("Data deleted");
								dropTables();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void export() {
		new AlertDialog.Builder(MainActivity.this)
				.setTitle("Export Data")
				.setMessage("How would you like to export the data?")
				.setPositiveButton("Flat file (CSV)",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								exportCSV();
							}

						})
				.setNeutralButton("Query file (SQL)",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								exportSQL();
							}

						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}

						}).show();
	}

	private void exportCSV() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1; // Months are numbered 0-11
		int day = c.get(Calendar.DAY_OF_MONTH);
		String date = year + "-" + month + "-" + day;

		if (year == 0) { // database is empty
			makeToast("Database currently empty.");
			return;
		}

		try {
			File myFile;
			myFile = new File("sdcard/Inventory/inventory_" + date + ".CSV");
			if (myFile.exists()) {
				for (int i = 0; i < 100; i++) // If the file already exists,
												// rename it
				{ // with a "_#" suffix
					date = Integer.toString(year) + "."
							+ Integer.toString(month) + "."
							+ Integer.toString(day);
					date += "_" + Integer.toString(i);
					myFile = new File("sdcard/inventory/inventory_" + date
							+ ".CSV");
					if (!myFile.exists()) {
						break;
					}
				}
				writeCSV("inventory_" + date + ".CSV");
			} else {
				writeCSV("inventory_" + date + ".CSV");
			}
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

		dropTables();
	}

	private void exportSQL() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1; // Months are numbered 0-11
		int day = c.get(Calendar.DAY_OF_MONTH);
		String date = year + "-" + month + "-" + day;

		if (year == 0) { // database is empty
			makeToast("Database currently empty.");
			return;
		}

		try {
			File myFile;
			myFile = new File("sdcard/Inventory/inventory_" + date + ".SQL");
			if (myFile.exists()) {
				for (int i = 0; i < 100; i++) // If the file already exists,
												// rename it
				{ // with a "_#" suffix
					date = Integer.toString(year) + "-"
							+ Integer.toString(month) + "-"
							+ Integer.toString(day);
					date += "_" + Integer.toString(i);
					myFile = new File("sdcard/inventory/inventory_" + date
							+ ".SQL");
					if (!myFile.exists()) {
						break;
					}
				}
				writeSQL("inventory_" + date + ".SQL");
			} else {
				writeSQL("inventory_" + date + ".SQL");
			}
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

		dropTables();
	}

	private void writeCSV(String filename) {
		String record;
		File myFile = null;
		list = db.getAllBooks();
		itemCount = list.size();
		if (itemCount != 0) {
			try {
				myFile = new File("sdcard/inventory/" + filename);
				myFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(myFile);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

				record = "box_id,seed_id,inventory_date,inventory_person,weight_gram\r\n";
				myOutWriter.append(record);

				for (int i = 0; i < itemCount; i++) {

					String[] temp = list.get(i).toString().split(",");
					record = temp[0] + ","; // box
					record += temp[1] + ","; // seed id
					record += temp[3] + ","; // date
					record += temp[2] + ","; // person
					record += temp[5] + "\r\n"; // weight

					myOutWriter.append(record);
				}
				myOutWriter.close();
				fOut.close();
				makeFileDiscoverable(myFile,this);
				makeToast("File exported successfully.");
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}

		}
		db.close();
		shareFile(filename);
		dropTables();
	}

	private void writeSQL(String filename) {
		String record;
		String boxList = "";
		File myFile = null;
		list = db.getAllBooks();
		itemCount = list.size();

		String[] boxes = db.getBoxList();

		if (itemCount != 0) {
			try {
				myFile = new File("sdcard/inventory/" + filename);
				myFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(myFile);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

				// get boxes
				for (int i = 0; i < boxes.length; i++) {
					if (i == boxes.length - 1 && boxes[i] != null) {
						boxList = boxList + "'" + boxes[i] + "'";
					} else if (boxes[i] != null) {
						boxList = boxList + "'" + boxes[i] + "'" + ",";
					}
				}

				record = "DELETE FROM seedinv WHERE seedinv.box_id in ("
						+ boxList + ");\n";
				record += "INSERT INTO seedinv(`box_id`,`seed_id`,`inventory_date`,`inventory_person`,`weight_gram`)\r\nVALUES";
				myOutWriter.append(record);

				for (int i = 0; i < itemCount; i++) {
					String[] temp = list.get(i).toString().split(",");

					for (int j = 0; j < temp.length; j++) {
						if (temp[j].length() == 0) {
							temp[j] = "null";
						}
					}

					record = "(";
					record += addTicks(temp[0]) + ","; // box
					record += addTicks(temp[1]) + ","; // seed id
					record += addTicks(temp[3]) + ","; // date
					record += addTicks(temp[2]) + ","; // person
					record += addTicks(temp[5]); // weight
					record += ")";
					
					if(i==itemCount-1){
						record+=";\r\n";
					} else {
						record+=",\r\n";
					}
					
					myOutWriter.append(record);
				}
				myOutWriter.close();
				fOut.close();
				makeFileDiscoverable(myFile,this);				
				
				makeToast("File exported successfully.");
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}

		}
		db.close();
		shareFile(filename);
		dropTables();
	}
	
	public void makeFileDiscoverable(File file, Context context){
		MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, null, null);
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)));
	}

	private String addTicks(String entry) {
		String newEntry;
		if(entry.contains("null")) {
			newEntry = "null";
		} else {
			newEntry = "'" + entry + "'";
		}
		return newEntry;
	}
	
	private void dropTables() {
		db.deleteAllBooks();
		InventoryTable.removeAllViews();
		currentItemNum = 1;
	}

	private void shareFile(String filePath) {
		filePath = "sdcard/inventory/" + filePath;
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filePath));
		try {
			startActivity(Intent.createChooser(intent, "Sending File..."));
		} finally {
		}
	}

	public void findScale() {
		if (mDevice == null) {
			UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
			HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			while (deviceIterator.hasNext()) {
				mDevice = deviceIterator.next();
				Log.v(TAG,
						String.format(
								"name=%s deviceId=%d productId=%d vendorId=%d deviceClass=%d subClass=%d protocol=%d interfaceCount=%d",
								mDevice.getDeviceName(), mDevice.getDeviceId(),
								mDevice.getProductId(), mDevice.getVendorId(),
								mDevice.getDeviceClass(),
								mDevice.getDeviceSubclass(),
								mDevice.getDeviceProtocol(),
								mDevice.getInterfaceCount()));
				break;
			}
		}

		if (mDevice != null) {
			mWeightEditText.setText("0");
			new ScaleListener().execute();
		} else {
			new AlertDialog.Builder(MainActivity.this)
					.setTitle("Scale Not Found")
					.setMessage(
							"Please connect scale with OTG cable and turn the scale on")
					.setCancelable(false)
					.setPositiveButton("Try Again",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									findScale();
								}

							})
					.setNegativeButton("Ignore",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}

							}).show();
		}
	}

	private class ScaleListener extends AsyncTask<Void, Double, Void> {
		private byte mLastStatus = 0;
		private double mLastWeight = 0;

		@Override
		protected Void doInBackground(Void... arg0) {

			byte[] data = new byte[128];
			int TIMEOUT = 2000;
			boolean forceClaim = true;

			Log.v(TAG, "start transfer");

			UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

			if (mDevice == null) {
				Log.e(TAG, "no device");
				return null;
			}
			UsbInterface intf = mDevice.getInterface(0);

			Log.v(TAG,
					String.format("endpoint count = %d",
							intf.getEndpointCount()));
			UsbEndpoint endpoint = intf.getEndpoint(0);
			Log.v(TAG, String.format(
					"endpoint direction = %d out = %d in = %d",
					endpoint.getDirection(), UsbConstants.USB_DIR_OUT,
					UsbConstants.USB_DIR_IN));
			UsbDeviceConnection connection = usbManager.openDevice(mDevice);
			Log.v(TAG, "got connection:" + connection.toString());
			connection.claimInterface(intf, forceClaim);
			while (true) {

				int length = connection.bulkTransfer(endpoint, data,
						data.length, TIMEOUT);

				if (length != 6) {
					Log.e(TAG, String.format("invalid length: %d", length));
					return null;
				}

				byte report = data[0];
				byte status = data[1];
				byte exp = data[3];
				short weightLSB = (short) (data[4] & 0xff);
				short weightMSB = (short) (data[5] & 0xff);

				// Log.v(TAG, String.format(
				// "report=%x status=%x exp=%x lsb=%x msb=%x", report,
				// status, exp, weightLSB, weightMSB));

				if (report != 3) {
					Log.v(TAG, String.format("scale status error %d", status));
					return null;
				}

				if(mDevice.getProductId()==519){
					mWeightGrams = (weightLSB + weightMSB * 256.0)/10.0;
				} else {
					mWeightGrams = (weightLSB + weightMSB * 256.0);	
				}
				double zWeight = (mWeightGrams - mZeroGrams);

				switch (status) {
				case 1:
					Log.w(TAG, "Scale reports FAULT!\n");
					break;
				case 3:
					Log.i(TAG, "Weighing...");
					if (mLastWeight != zWeight) {
						publishProgress(zWeight);
					}
					break;
				case 2:
				case 4:
					if (mLastWeight != zWeight) {
						Log.i(TAG, String.format("Final Weight: %f", zWeight));
						publishProgress(zWeight);
					}
					break;
				case 5:
					Log.w(TAG, "Scale reports Under Zero");
					if (mLastWeight != zWeight) {
						publishProgress(0.0);
					}
					break;
				case 6:
					Log.w(TAG, "Scale reports Over Weight!");
					break;
				case 7:
					Log.e(TAG, "Scale reports Calibration Needed!");
					break;
				case 8:
					Log.e(TAG, "Scale reports Re-zeroing Needed!\n");
					break;
				default:
					Log.e(TAG, "Unknown status code");
					break;
				}

				mLastWeight = zWeight;
				mLastStatus = status;
			}
		}

		@Override
		protected void onProgressUpdate(Double... weights) {
			Double weight = weights[0];
			Log.i(TAG, "update progress");
			String weightText = String.format("%.1f", weight);
			Log.i(TAG, weightText);
			mWeightEditText.setText(weightText);
			mWeightEditText.invalidate();
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(getApplicationContext(), "Scale Disconnected",
					Toast.LENGTH_LONG).show();
			mDevice = null;
			mWeightEditText.setText("Not connected");
		}
	}

	public void onInit(int status) {

	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
