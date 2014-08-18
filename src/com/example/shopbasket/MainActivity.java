package com.example.shopbasket;

import com.example.shorbasket.R;

import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;


/*
 * 24,04 Проэкт сука нехило затянулся
 * надо разобраться с созданием лоадеров в локальной базе
 * без ContentProveder.
 * Точнее с multiLoaders 
 * На 25,04 Добисать адаптер. Разобраться с children и group. 
 * Как они формируються по какому предикату определяеться что group это group.
 * Или может быть это я сам формирую при выборке из таблицы ????? 
 * Типо первый столбец это и  есть group????
 * 
 *  C group разобрался: сам придумываешь что будет group а что child
 *  сталось разобраться с multiloaders without ContentProvider
 *  
 *  
 *  МультиЛоадер не нужен так как за загрузку эл-тов списка (детей) отвечает адаптер
 *  какето-то трабла с передачей строки в на поист в DataBaseIO
 *  
 *    Решил надо еще на передавать на адаптер id из базы (для групп и детей)
 *    
 *    Трабла с кликом на детях нажимаю на одном отображаеться + отображаеться и на других(некоторых)
 *    зависимость не установил
 *    Попробовать сделать массив id(из метода OnChildClick) нажатых детей и по нему сравнивать был ли нажат
 *    или нет.
 *    
 *    Попробовать потестить на планшете нажатие детей
 *    
 */




public class MainActivity extends Activity implements LoaderCallbacks<Cursor>{
	
	ExpandableListView eListView;
	LoaderManager mLoaderManager;
	private static DataBaseIO mDataBaseIO;
	private PurchaseAdapter mPurchaseAdapter;
	private Parcelable state;
	
	//Constant for Loader
	private static final int GROUP_LOADER = 1;
	private static final String TAG = "ShopBasket Main";
	
	//This array need to initial and create adapter
	String[] groupFrom = new String [] {PurchaseDataBase.DATE_PURCHASE};
	int[] groupTo = new int [] {R.id.tvCollapsedList};
	
	
	String[] childFrom = new String [] {PurchaseDataBase.ITEM_NAME};
	int[] childTo = new int[] {R.id.itemName};
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mDataBaseIO = new DataBaseIO(this);// initial mDataBaseIO
		mDataBaseIO.openDB();//open or create DataBase
		mLoaderManager= getLoaderManager();  //initial mLoaderManager
		
			
		// Get Cursor Object to construct SimpleCursorTreeAdapter
		Cursor c = mDataBaseIO.getAllPurchasesCursor();
		Log.i(TAG, c.toString());
		
	
		
		//Initial SimpleCursorTreeAdapter
		mPurchaseAdapter = new PurchaseAdapter(this, c, R.layout.collapsed_group, 
				R.layout.expanded_group, groupFrom, groupTo, R.layout.item_layout, childFrom, childTo);
		
		 mLoaderManager.initLoader(GROUP_LOADER,null, this);
		 
	
		
		
		//find ExpandableListView
		eListView = (ExpandableListView)findViewById(R.id.eListView);
		//Set the Adapter
		eListView.setAdapter(mPurchaseAdapter);
	
		
		//Set ContextMenutoExpandableListView
		registerForContextMenu(eListView);
		//Save aplication state
		state = eListView.onSaveInstanceState();
	}
	
	//Create ContextMenu for ExpandableList with "tap" ONLY group item
	
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuinfo) {
		super.onCreateContextMenu(menu, view, menuinfo);
		
		MenuInflater mMenuInflater = getMenuInflater();
		
		ExpandableListView.ExpandableListContextMenuInfo elMenuInfo = 
				(ExpandableListView.ExpandableListContextMenuInfo) menuinfo;
		
		int whereTap = ExpandableListView.getPackedPositionType(elMenuInfo.packedPosition);
				
		//Check for tap in group, NOT in children (whereTap for group ALWAYS = 0)
		if(whereTap == 0)		
		mMenuInflater.inflate(R.menu.context_group_menu, menu);
		
	}
	//Work with ContextMenu
	public boolean onContextItemSelected (MenuItem menuItem){
	
		ExpandableListView.ExpandableListContextMenuInfo elMenuInfo = 
				(ExpandableListView.ExpandableListContextMenuInfo) menuItem.getMenuInfo();
		int groupPositioninContextMenu = ExpandableListView.getPackedPositionGroup(elMenuInfo.packedPosition);
		Log.i(TAG, "Position in List: "+ String.valueOf(groupPositioninContextMenu));
		
		switch (menuItem.getItemId()) {
		case R.id.delete_bag:
			Cursor c = mDataBaseIO.getAllPurchasesCursor();
			c.moveToPosition(groupPositioninContextMenu);
			String datetoDelete = c.getString(c.getColumnIndex(PurchaseDataBase.DATE_PURCHASE));
			mDataBaseIO.deletePurchaseBagbyDate(datetoDelete);
			mLoaderManager.getLoader(GROUP_LOADER).forceLoad();
			mPurchaseAdapter.notifyDataSetChanged();
			Toast.makeText(MainActivity.this, "Bag "+datetoDelete+" was deleted", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
		return true;
		}
	
	
	
	
	protected void onStart(){
		super.onStart();
		eListView.onRestoreInstanceState(state);
		mDataBaseIO.openDB();
		mLoaderManager.getLoader(GROUP_LOADER).forceLoad();
		}
	protected void onResume() {
		super.onResume();
		eListView.onRestoreInstanceState(state);
		mDataBaseIO.openDB();
		mLoaderManager.getLoader(GROUP_LOADER).forceLoad();
	}
	
	protected void onStop(){
		Log.i(TAG, "in MAin onStop");
		super.onStop();
		}
	protected void onDestroy() {
		
		mLoaderManager.destroyLoader(GROUP_LOADER);
		mDataBaseIO.closeDB();
		super.onDestroy();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected (MenuItem item){
		
		switch(item.getItemId()){
		/*
		 * Exit from APP
		 * */
		case R.id.exit:
			Toast.makeText(getApplicationContext(), "Exit", Toast.LENGTH_SHORT).show();
			finish();
			break;
			
		//Ceate new shopBag, go to ItemList Activity
		case R.id.create_new_list:
			Intent newItemListIntent = new Intent(this, ItemList.class);
			startActivityForResult(newItemListIntent, RESULT_OK);
			break;
			
		default:
			break;
		}
		
		return true;
		
	}
	
	//ADAPTER
	
	public class PurchaseAdapter extends SimpleCursorTreeAdapter{
		
		private static final String TAG = "PurchaseAdapter";
		private static final float CHECKED = 1f;
		private static final float UNCHEKED = 0f;
		
		private LayoutInflater mLayoutInflater;
		private ChildViewHolder mChildViewHolder;
		
	
		

		public PurchaseAdapter(Context context, Cursor cursor,
				int collapsedGroupLayout, int expandedGroupLayout,
				String[] groupFrom, int[] groupTo, int childLayout,
				String[] childFrom, int[] childTo) {
			super(context, cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom,
					groupTo, childLayout, childFrom, childTo);
			// TODO Auto-generated constructor stub
			mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

					}
		
		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			// TODO Auto-generated method stub
			 int idGroupDate = groupCursor.getPosition();
			 Log.i(TAG, "childrenCursor for group"+ String.valueOf(idGroupDate));
			 String dateForChild = groupCursor.getString(groupCursor.getColumnIndex(PurchaseDataBase.DATE_PURCHASE));
			 Log.i(TAG, dateForChild);
					 
			 //for TEST
			 Cursor cursor = mDataBaseIO.getPurchasebyDate(dateForChild);
			
			Log.i(TAG, "Cursor has row: "+ String.valueOf(cursor.getCount())+" columns: "+String.valueOf(cursor.getColumnCount())+" colunmIndex: "+
			 String.valueOf(cursor.getColumnIndex(PurchaseDataBase.ITEM_NAME)));
			 cursor.moveToFirst();
			 do {
				 Log.i(TAG, cursor.getString(cursor.getColumnIndex(PurchaseDataBase.ITEM_NAME)));
			 }while(cursor.moveToNext());
			 cursor.close();
			 //end TEST block
			
			return mDataBaseIO.getPurchasebyDate(dateForChild);
		}
		
		public void bindChildView (View view,  final Context context, Cursor cursor, boolean isLastChild){
			super.bindChildView(view, context, cursor, isLastChild);
			
			Log.i(TAG, "Inside bindChildView()");
			mChildViewHolder = (ChildViewHolder)view.getTag();
			
			final float itemStatus =cursor.getFloat(cursor.getColumnIndex(PurchaseDataBase.PURCHASE_STATUS));
			final int id = cursor.getInt(cursor.getColumnIndex(PurchaseDataBase.KEY_ID_ITEM));
			Log.i(TAG, "id: "+ id+" status: "+itemStatus);
			
			
			mChildViewHolder.currentchildImageView.setAlpha(itemStatus);
			
			if(itemStatus==CHECKED){
				// Set Text in TEXTVIEW striked install pain flag
				mChildViewHolder.currentchildTextView.setPaintFlags(mChildViewHolder.currentchildTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}else{
				mChildViewHolder.currentchildTextView.setPaintFlags(mChildViewHolder.currentchildTextView.getPaintFlags() &(~ Paint.STRIKE_THRU_TEXT_FLAG));
			}
			
					view.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if (itemStatus == CHECKED){
								mDataBaseIO.setStatusOfPurchaseItem((int)UNCHEKED, id);
								Toast.makeText(context, "I not buy this yet", Toast.LENGTH_SHORT).show();
								//Set invisible image
								mChildViewHolder.currentchildImageView.setAlpha(UNCHEKED);
								
								mChildViewHolder.currentchildTextView.setPaintFlags(mChildViewHolder.currentchildTextView.getPaintFlags() &(~ Paint.STRIKE_THRU_TEXT_FLAG));
								
								mLoaderManager.getLoader(GROUP_LOADER).forceLoad();
								mPurchaseAdapter.notifyDataSetChanged();
							}
							else{
							mDataBaseIO.setStatusOfPurchaseItem((int)CHECKED, id);
							Toast.makeText(context, "I buy this now", Toast.LENGTH_SHORT).show();
							//Set visible image
							mChildViewHolder.currentchildImageView.setAlpha(CHECKED);

							//setCross text
							mChildViewHolder.currentchildTextView.setPaintFlags(mChildViewHolder.currentchildTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

							mLoaderManager.getLoader(GROUP_LOADER).forceLoad();
							mPurchaseAdapter.notifyDataSetChanged();

									}
							
						}
					});
		}
		public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent){
			
			Log.i(TAG, "Inside newChildView()");
			ChildViewHolder mChildViewHolder = new ChildViewHolder();
			View currentChildView = mLayoutInflater.inflate(R.layout.item_layout, parent, false);
			mChildViewHolder.currentchildTextView = (TextView) currentChildView.findViewById(R.id.itemName);
			mChildViewHolder.currentchildImageView = (ImageView) currentChildView.findViewById(R.id.itemstatus_img);
			
			currentChildView.setTag(mChildViewHolder);
			mChildViewHolder.currentchildTextView.setTag(cursor.getPosition());
			Log.i(TAG, "CursorPos "+ String.valueOf(cursor.getPosition()));
			
			return currentChildView;
			}
		
		

	}
	static class ChildViewHolder{
		protected TextView currentchildTextView;
		protected ImageView currentchildImageView;

	}

	
	
	
	//Loader Method
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onCreateLoader");
		return new MyCursorFromCursorLoader(this, mDataBaseIO);
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
		if (mPurchaseAdapter !=null && data != null){
		
				mPurchaseAdapter.setGroupCursor(data);				
		}//end IF
		Log.i(TAG, "onLoadFinish use setGrourCursor");
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		if(mPurchaseAdapter != null)
			mPurchaseAdapter.setGroupCursor(null);
		Log.i(TAG, "onLoadReset");
	}
	
	static class MyCursorFromCursorLoader extends CursorLoader {
		
		
		private DataBaseIO dbIO;
		
		public MyCursorFromCursorLoader(Context context, DataBaseIO dbIO) {
			super(context);
			// TODO Auto-generated constructor stub
			this.dbIO=mDataBaseIO;
						
		}
		public Cursor loadInBackground(){
				Log.i(TAG, "LoadInBackground 1");
				return dbIO.getAllPurchasesCursor();
				}

	
	}
	
	
}
