package com.example.shopbasket;

import java.util.Date;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataBaseIO {

	private PurchaseDataBase mPurchaseDB;
	private SQLiteDatabase db;
	
	
	//CONSTRACTOR
	public DataBaseIO (Context context){
		mPurchaseDB = new PurchaseDataBase(context);
	}

	//Tag to LogCat
	private static final String TAG = "ShopBasket DataBaseIO.class";



	//Open DATABASE TO WRITE	
	public boolean openDB() {
		Log.i(TAG, "Try to open DB");
		if(mPurchaseDB != null){
			db = mPurchaseDB.getWritableDatabase();
			return true;
	}
		return false;

	}

	//Close the connection to DB
	public void closeDB(){
		Log.i(TAG, "Close DB");
		mPurchaseDB.close();
	}

	/* Methods to work with data in Db
	 *  
	 * add, getAll, get with condition and delete item in ITEM_TABLE
	 * 
	 * add, getAll, get with condition and delete and sort data in PurchaceTable
	 *  
	 */

	//Work with ITEM_TABLE
/*
 * ADD to ITEM_TABLE
 * 
 * */
	public void addItemName(String ItemName){
		ContentValues conval = new ContentValues();
		conval.put(PurchaseDataBase.ITEM_NAME,ItemName);
		conval.put(PurchaseDataBase.ITEM_STATUS, 0);
		db.insert(PurchaseDataBase.TABLE_ITEM, null, conval);
		Log.i(TAG, "New item "+ ItemName+" is added");
	}

	public void addItemStatus(int status, int updateID){
		String updateSQLRequest;
		
			 updateSQLRequest = "UPDATE "+PurchaseDataBase.TABLE_ITEM+" SET "+PurchaseDataBase.ITEM_STATUS+"="+ String.valueOf(status)+" WHERE "+
					PurchaseDataBase.KEY_ID_ITEM+"="+ String.valueOf(updateID);
					
		
		Log.i(TAG, updateSQLRequest);
		
		db.execSQL(updateSQLRequest);
	}
/*
 * 
 * Get from ITEM_TABLE 
 * 
 * */
	
	public Cursor getAllItemCursor(){
		
		String selectionRequest = "SELECT * FROM "+PurchaseDataBase.TABLE_ITEM+" ORDER BY "+PurchaseDataBase.ITEM_NAME;
		//Cursor c = db.query(PurchaseDataBase.TABLE_ITEM, null, null, null, null, null, PurchaseDataBase.ITEM_NAME);
		return db.rawQuery(selectionRequest, null);
	}
	
	
	
	/*
	 * Method convert int status in DataBase in object's boolean status 
	 * */
	
	public boolean intToBoolean (int arg){
		
		if (arg == 1) return true;
		

		return false;
	}
/*
 * ����������� ��������� ��������� �� ������� � ���� � � ������
 * �������� ��� �������� �������� �� ����
 * */
	
	
	/*
	 * Rename single Item in ItemTable */
	public void renameItemName(String newName, int updateID){
		String updateSQLRequest = "UPDATE "+PurchaseDataBase.TABLE_ITEM+" SET "+PurchaseDataBase.ITEM_NAME+"="+ "'"+newName+"'"+" WHERE "+
								PurchaseDataBase.KEY_ID_ITEM+"="+ String.valueOf(updateID);
		Log.i(TAG, updateSQLRequest);
		db.execSQL(updateSQLRequest);
	}
	
/*
 * 
 * Delete from ITEM_TABLE
 * 
 * */
	
	public void clearTableItem(){
		
		Log.i(TAG, "Clear Table Item");
		db.delete(PurchaseDataBase.TABLE_ITEM, null, null);

		
	}

	
	public void deleteSingleItem(int indexInList){
		Log.i(TAG, "Delete Single Item from Table");
		
		db.delete(PurchaseDataBase.TABLE_ITEM, PurchaseDataBase.KEY_ID_ITEM + " = " + indexInList, null);

	}
	
	public void statusToDefalt() {
	String	updateSQLRequest = "UPDATE "+PurchaseDataBase.TABLE_ITEM+" SET "+PurchaseDataBase.ITEM_STATUS+"="+ "0"+" WHERE "+
				PurchaseDataBase.ITEM_STATUS+"="+ "1";
	db.execSQL(updateSQLRequest);

		
	}
	
	/*End work method with TABLE_ITEM
	 * 
	 * 
	 * 
	 * Method to work with PURCHASE_TABLE*/
	
	/*
	 * Add Purchase 
	 * 
	 * where selectedIDs ArrayList<> of selected _id from Table_Item
	 * where date contains date put in DatePicker Dialog  */
	
	public void addPurchase(Date date, ArrayList<Integer> selectedIDs ) {
		ContentValues conval = new ContentValues();
		if(!selectedIDs.isEmpty())
		db.beginTransaction();
		try{
		for (Integer selectedID:selectedIDs){
			conval.put(PurchaseDataBase.DATE_PURCHASE,date.toString());
			conval.put(PurchaseDataBase.PURCHASE_FROM_ITEM, selectedID);
			conval.put(PurchaseDataBase.PURCHASE_STATUS, 0);
			db.insert(PurchaseDataBase.TABLE_PURCHASE, null, conval);
			// For Test's
			Log.i(TAG, "Insert one row: date="+date.toString()+" idItem= "+String.valueOf(selectedID));
		}
		db.setTransactionSuccessful();
		}finally {
		db.endTransaction();
		}
	}
	
	
	
	// getTestCursor need to test
	public Cursor getTestCursor(){
		
		Cursor c = db.query(PurchaseDataBase.TABLE_PURCHASE, null, null, null, null, null, null);
		return c;
	}
	
	
	// Get all record to cursor from PURCHASE_TABLE
	public Cursor getAllPurchasesCursor() {
		
		String sqliteQuery = "SELECT "+PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.KEY_ID_PURHCASE+", "
									  +PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.DATE_PURCHASE+", "+
									   PurchaseDataBase.TABLE_ITEM+"."+PurchaseDataBase.ITEM_NAME+", "+
									  PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.PURCHASE_STATUS+
									  " FROM "+PurchaseDataBase.TABLE_PURCHASE+" INNER JOIN "+PurchaseDataBase.TABLE_ITEM+" ON "+
									  PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.PURCHASE_FROM_ITEM+"="+
									  PurchaseDataBase.TABLE_ITEM+"."+PurchaseDataBase.KEY_ID_ITEM+" GROUP BY "+
									  PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.DATE_PURCHASE;
										
		
				
		return db.rawQuery(sqliteQuery, null);
				
	}
	
	public Cursor getPurchasebyDate(String dateforGetChild){
	
		/*
		 * может заругаться на отсутствие двойных кавычек при передачи строкового параметра в базу
		 * последняя строка запроса dateforGetChild*/
		Log.i(TAG, dateforGetChild);
		
		String sqliteQuery = "SELECT "+PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.KEY_ID_PURHCASE+", "
									   /*PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.DATE_PURCHASE+", "*/+
				   					   PurchaseDataBase.TABLE_ITEM+"."+PurchaseDataBase.ITEM_NAME+", "+
				   					   PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.PURCHASE_STATUS+
				   					   " FROM "+PurchaseDataBase.TABLE_PURCHASE+" INNER JOIN "+PurchaseDataBase.TABLE_ITEM+" WHERE "+
				   					   PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.PURCHASE_FROM_ITEM+"="+
				   					   PurchaseDataBase.TABLE_ITEM+"."+PurchaseDataBase.KEY_ID_ITEM+" AND "+
				   					   PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.DATE_PURCHASE+
				   					   " = "+ "'"+dateforGetChild+"'";
		
	
		return db.rawQuery(sqliteQuery, null);
		
	}
	public void deletePurchaseBagbyDate (String date){
		db.delete(PurchaseDataBase.TABLE_PURCHASE, PurchaseDataBase.DATE_PURCHASE+"="+ "'"+date+"'", null);
	}
	/*
	 * status and itemID get from MainActivity in onClickChild()
	 */
	public void setStatusOfPurchaseItem (int status, long itemID){
		String updateSQLRequest;
		
		updateSQLRequest = "UPDATE "+PurchaseDataBase.TABLE_PURCHASE+" SET "+PurchaseDataBase.PURCHASE_STATUS+"="+ String.valueOf(status)+" WHERE "+
				PurchaseDataBase.KEY_ID_PURHCASE+"="+ String.valueOf(itemID);
			
		db.execSQL(updateSQLRequest);
		
	}
	public boolean getStatusofPurchaseItem(long itemId){
		int statusINT;
		String getCurrentStatus = "SELECT "+PurchaseDataBase.TABLE_PURCHASE+"."+PurchaseDataBase.PURCHASE_STATUS+" FROM "+PurchaseDataBase.TABLE_PURCHASE
				+" WHERE "+PurchaseDataBase.KEY_ID_PURHCASE+"="+String.valueOf(itemId);
		Cursor c = db.rawQuery(getCurrentStatus, null);
		c.moveToFirst();
		do{
			statusINT = c.getInt(c.getColumnIndex(PurchaseDataBase.PURCHASE_STATUS));
			Log.i(TAG, "Status fromcursor: "+ String.valueOf(statusINT));

		}while(c.moveToNext());
		
		return intToBoolean(statusINT);
		
	}
	
	
	
}
