package com.example.shopbasket;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;



import java.util.Locale;

import com.example.shorbasket.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;









@SuppressLint("ValidFragment")
public class ItemList extends Activity implements LoaderCallbacks<Cursor> {

	private static DataBaseIO mDataBaseIO;
	//private SimpleCursorAdapter mSimpleCursorAdapter;
	private ItemAdapter mItemAdapter;
	LoaderManager loadermanager;
	private Parcelable stateList;

	public ArrayList<Integer> selectedItemID = new ArrayList<Integer>();  // ������ ��� _id ��������� ��-���

	// CONSTANT
	//CursorLoader ID to ItemList
	private static final int ITEMLISTLOADER_ID = 1;


	private String newItemName, currentItemName;
	private int currentItemID; // ���������� ���������� ��� �������� � �������� _id ��-��, ������� ���� �� ��������������.
	// Dialog to add Item
	DialogFragment maddDialog, renameDialog;

	//ListView to display the ItemList
	ListView lvItemList;

	Locale currentLocale;


	//My log
	private static final String TAG = "ShopBasket ItemList.class";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_list);

		//Get currentLocale
		currentLocale = getResources().getConfiguration().locale;
		Log.i(TAG, currentLocale.toString());

		//Cerate and open DATABASE
		mDataBaseIO = new DataBaseIO(this);
		mDataBaseIO.openDB();

		//Get LoaderManager
		loadermanager = getLoaderManager();

		//Create  CursorAdapter
		mItemAdapter = new ItemAdapter(this, null, 0);

		//Initialise the Loader
		loadermanager.initLoader(ITEMLISTLOADER_ID, null, this);

		//Find ListView and set SimpleCursorAdapter to listView
		lvItemList = (ListView) findViewById(R.id.item_listView);
		lvItemList.setAdapter(mItemAdapter);
		lvItemList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		

		//register ListView for ContextMenu
		registerForContextMenu(lvItemList);

		stateList = lvItemList.onSaveInstanceState();
	

		//Create add Dialog and RenameDialog
		maddDialog = new AddDialog();
		renameDialog = new RenameDialog();


	}
	
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_item_menu, menu);
	}

	public boolean onContextItemSelected (MenuItem menuitem){

		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuitem.getMenuInfo();
		Cursor oneCursorRecord = (Cursor) lvItemList.getItemAtPosition(acmi.position);
		currentItemID = oneCursorRecord.getInt(oneCursorRecord.getColumnIndex(PurchaseDataBase.KEY_ID_ITEM));
		currentItemName = oneCursorRecord.getString(oneCursorRecord.getColumnIndex(PurchaseDataBase.ITEM_NAME));

		//��� ������
		Log.i(TAG, "Current Name is "+currentItemName+" CurrentID is "+String.valueOf(currentItemID)+" Cheked possition: "+ String.valueOf(acmi.position));


		//�������� ����� ���� �� ID � �����
		switch (menuitem.getItemId()){

		case R.id.rename:
			//������� ������ � ����, � ������ ������ ��� ��������
			renameDialog.show(getFragmentManager(), "renameDialog");
			Log.i(TAG, "Updated");
			break;

		case R.id.delete:
			//������� ������ �� ����. _id ���������� ��-�� �������� ����� acmi.position+1
			mDataBaseIO.deleteSingleItem(currentItemID);
			loadermanager.getLoader(ITEMLISTLOADER_ID).forceLoad();
			if (!selectedItemID.isEmpty())
				selectedItemID.remove((Integer)currentItemID);
			Log.i(TAG, "Deleted from DB and from Array");
			break;
		}

		return super.onContextItemSelected(menuitem);

	}
	protected void onStart(){
		super.onStart();
		lvItemList.onRestoreInstanceState(stateList);
		mDataBaseIO.openDB();
		loadermanager.getLoader(ITEMLISTLOADER_ID).forceLoad();
		}
	protected void onResume() {
		super.onResume();
		lvItemList.onRestoreInstanceState(stateList);
		mDataBaseIO.openDB();
		loadermanager.getLoader(ITEMLISTLOADER_ID).forceLoad();
	}

	protected void onStop(){
		Log.i(TAG, "onStop() invoke");
		mDataBaseIO.statusToDefalt();
		//mDataBaseIO.closeDB();
		super.onStop();
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_item, menu);
		return true;
	}

	public boolean onOptionsItemSelected (MenuItem menuitem){

		switch(menuitem.getItemId()){
		case R.id.add_item:
			maddDialog.show(getFragmentManager(), "addDialog");
			break;

		case R.id.create_new_bag:

			if(selectedItemID.isEmpty()){
				Log.i(TAG, "Array is empty");
				Toast.makeText(getApplicationContext(), "Bag is not be empty", Toast.LENGTH_SHORT).show();
			}
			else{ 
				showDatePicker();

			}break;
			
		case R.id.clear_selection:
			mDataBaseIO.statusToDefalt();
			loadermanager.getLoader(ITEMLISTLOADER_ID).forceLoad();
			if(!selectedItemID.isEmpty())
				selectedItemID.clear();
			break;
			
		case R.id.clear_table:
			AlertDialog.Builder alertbuilder = new AlertDialog.Builder(this);
			//setTitle
			alertbuilder.setTitle(R.string.alertdiolog_title);
			//set Alert Dialog view 
			alertbuilder.setMessage(R.string.alertdialog_message)
						.setCancelable(false)
						.setPositiveButton("Yeas", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								mDataBaseIO.clearTableItem();
								loadermanager.getLoader(ITEMLISTLOADER_ID).forceLoad();
							}
						})
						.setNegativeButton("No", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}
						});
			AlertDialog alertDialog=alertbuilder.create();
			alertDialog.show();
			break;

		default: break;
		}


		return true;

	}
	

	//Create Dialog to Update Item  (ITEM_NAME)

	public class RenameDialog extends DialogFragment{

		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			getDialog().setTitle("Rename "+currentItemName+" to ");
			View dialogView = inflater.inflate(R.layout.fragment_rename_item_name, null);

			//Initial Button in DialogFragment
			final Button butOkRename = (Button) dialogView.findViewById(R.id.butOkRename);
			Button butCancelRemane = (Button) dialogView.findViewById(R.id.butCancelRename);
			final EditText etRename = (EditText) dialogView.findViewById(R.id.etRename);

			//Set onclickListener on butAdd
			butOkRename.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.i(TAG, etRename.getText().toString());
					if(etRename.getText().toString().isEmpty()){
						Toast.makeText(getApplicationContext(), "Name is not be empty", Toast.LENGTH_SHORT).show();
						Log.i(TAG, etRename.getText().toString()+"is EMPTY");
					} else {
						String renameItemName = etRename.getText().toString();

						mDataBaseIO.renameItemName(renameItemName, currentItemID);

						loadermanager.getLoader(ITEMLISTLOADER_ID).forceLoad();

						dismiss();
					}
				}

			});

			//Set onClickListener on butCancel
			butCancelRemane.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dismiss();
				}
			});

			return dialogView;
		}

	}


	//Create Dialog to add Item
	public class AddDialog extends DialogFragment{

		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			getDialog().setTitle("Create new item");
			View dialogView = inflater.inflate(R.layout.fragment_add_item_name, null);

			//Initial Button in DialogFragment
			final Button butAdd = (Button) dialogView.findViewById(R.id.butAdd);
			Button butCancel = (Button) dialogView.findViewById(R.id.butCancel);

			//Initial InputText Field
			final EditText etNewName = (EditText) dialogView.findViewById(R.id.editText1);
			etNewName.setText("");



			//Set onclickListener on butAdd
			butAdd.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.i(TAG, etNewName.getText().toString());
					if(etNewName.getText().toString().isEmpty()){
						Toast.makeText(getApplicationContext(), "Name is not be empty", Toast.LENGTH_SHORT).show();
						Log.i(TAG, etNewName.getText().toString()+"is EMPTY");
					} else {
						newItemName = etNewName.getText().toString();

						mDataBaseIO.addItemName(newItemName);
						//loadermanager.getLoader(ITEMLISTLOADER_ID).forceLoad();
						etNewName.setText("");
						loadermanager.getLoader(ITEMLISTLOADER_ID).forceLoad();
						dismiss();
												
					}
				}

			});

			//Set onClickListener on butCancel
			butCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dismiss();
				}
			});

			return dialogView;
		}

	}

	//Create SETDATE Dialog
	public class SetDateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener{

		boolean oneInvoke = true;


		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance(currentLocale);
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			if(oneInvoke){
				oneInvoke = false;
				Log.i(TAG, "Year: "+String.valueOf(year)+" Month: "+String.valueOf(monthOfYear)+" Day: "+String.valueOf(dayOfMonth));
				returnSettedDate(year, monthOfYear, dayOfMonth);
				Log.i(TAG, returnSettedDate(year, monthOfYear, dayOfMonth).toString());
				mDataBaseIO.addPurchase(returnSettedDate(year, monthOfYear, dayOfMonth), selectedItemID);
				selectedItemID.clear();
				getActivity().setResult(RESULT_OK);
				getActivity().finish();
				dismiss();

			}

		}
		public Date returnSettedDate (int year, int monthOfYear,
				int dayOfMonth){
			Date inDialogSettedDate = new Date();
			Calendar setedCalendar = Calendar.getInstance(currentLocale);
			setedCalendar.set(year, monthOfYear, dayOfMonth);
			inDialogSettedDate.setTime(setedCalendar.getTimeInMillis());

			return inDialogSettedDate;

		}

	}


	private void showDatePicker(){
		DialogFragment setDateDialog = new SetDateDialog();
		setDateDialog.show(getFragmentManager(), "setDate");

	}









	/*
	 * Method to work with loader*/


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub

		return new MyLoaderCursortoCursor(this, mDataBaseIO);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		// TODO Auto-generated method stub
		if(mItemAdapter != null && cursor != null){
			mItemAdapter.swapCursor(cursor);
			Log.i(TAG, "onLoadFinished: "+cursor.toString());
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		if(mItemAdapter != null)
			mItemAdapter.swapCursor(null);
		else Log.i(TAG, "onLoaderReset mSimpleCursorAdapter is null");

	}

	//Inner class implemented cursor loader need to convert Loader<Cursor> to Cursor
	static class MyLoaderCursortoCursor extends CursorLoader {

		private DataBaseIO dbIO;

		public MyLoaderCursortoCursor(Context context, DataBaseIO dbIO) {
			super(context);
			// TODO Auto-generated constructor stub
			this.dbIO = mDataBaseIO;

		}

		public Cursor loadInBackground(){
			Cursor cursor = dbIO.getAllItemCursor();
			return cursor;

		}

	}
	// Holder class need to hold information from ItemAdapter.newView() to build  view

	static class ViewHolder{

		protected TextView currentTextView;
		protected ImageView currentImageView;
	}


	//Item Adapter class create the adapter
	class ItemAdapter extends CursorAdapter{

		private LayoutInflater mLayoutInflater;
		private static final String TAG = "ItemAdapter.class Inner";
		private int position;
		private ViewHolder mbindViewHolder;

		public ItemAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);

			mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			Log.i(TAG, "Invoke bindView()");
			currentItemID =0;

			mbindViewHolder = (ViewHolder) view.getTag();
			
			//Input data from cursor to view(saved view get from HolderView)
			mbindViewHolder.currentTextView.setText(
					cursor.getString(cursor.getColumnIndex(PurchaseDataBase.ITEM_NAME)));
			
			mbindViewHolder.currentImageView.setAlpha(cursor.getFloat(cursor.getColumnIndex(PurchaseDataBase.ITEM_STATUS)));
			
			//This variable need to get access in database record in ClickListener
			currentItemID = cursor.getInt(cursor.getColumnIndex(PurchaseDataBase.KEY_ID_ITEM));
			int statusitem = cursor.getInt(cursor.getColumnIndex(PurchaseDataBase.ITEM_STATUS));
			
			///Set different surface to TextView(which position checked or not)
			if(statusitem == 1){
				mbindViewHolder.currentTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				
			}else{
				mbindViewHolder.currentTextView.setTypeface(null, Typeface.NORMAL);
				
			}
			
			
			mbindViewHolder.currentTextView.setOnClickListener(new MyTextViewListener(mbindViewHolder.currentTextView,
					cursor.getPosition(), currentItemID, statusitem));
			
		
			Log.i(TAG, mbindViewHolder.currentTextView.getText()+ " status: "+ String.valueOf(statusitem));
			Log.i(TAG, String.valueOf(mbindViewHolder.currentTextView.getTag(position)));
			Log.i(TAG, "Count: "+ String.valueOf(cursor.getCount()));



		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			Log.i(TAG, "Invoke newView()");
			// TODO Auto-generated method stub
			ViewHolder mViewHolder = new ViewHolder();
			View currentView = mLayoutInflater.inflate(R.layout.item_layout,parent,false);
			mViewHolder.currentTextView = (TextView) currentView.findViewById(R.id.itemName);
			
			mViewHolder.currentImageView = (ImageView) currentView.findViewById(R.id.itemstatus_img);

			mViewHolder.currentTextView.setTag(cursor.getPosition());

			currentView.setTag(mViewHolder);
			return currentView;
		}

		class MyTextViewListener implements View.OnClickListener {
			
			final static int CHECKED = 1;
			final static int UNCHECKED = 0;
			
			View mview;
			int curposition;
			int currentIDforItem;
			int itemISchecked;


			public MyTextViewListener(View v, int position, int currentID, int itemstatus) {
				// TODO Auto-generated constructor stub

				this.mview = v;
				this.curposition = position;
				this.currentIDforItem = currentID;
				this.itemISchecked = itemstatus;
			}

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i(TAG, "InsideOnClick v.getTag "+ String.valueOf((Integer)v.getTag()));
				Log.i(TAG, "Inside Onclick position "+String.valueOf(this.curposition));
				Log.i(TAG, "CurrentID in Onclick: "+String.valueOf(currentIDforItem));
							
						if(itemISchecked==1){
							mDataBaseIO.addItemStatus(UNCHECKED, currentIDforItem);
							selectedItemID.remove((Integer)currentIDforItem);
													}else{
							mDataBaseIO.addItemStatus(CHECKED, currentIDforItem);
							selectedItemID.add(currentIDforItem);

						}
					//Log.i(TAG, "status in onClick "+String.valueOf(mbindViewHolder.currentCheckBox.isChecked()));
					loadermanager.getLoader(ITEMLISTLOADER_ID).forceLoad();
					Log.i(TAG, "Array: "+ selectedItemID.toString());
			}
		}


	}


}



