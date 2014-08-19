package com.example.shopbasket;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PurchaseDataBase extends SQLiteOpenHelper{
	
	

	//Tag to LogCat
	private static final String TAG = "ShopBasket PurcaseDB.class";
	
	//DataBase version
	private static final int DATABASE_VERSION = 1;
	
	//DataBase name
	private static final String DATABASE_NAME = "purchaseDataBase";
	
	//Table's Name
	public static final String TABLE_ITEM = "itemTable";
	public static final String TABLE_PURCHASE = "purchaseTable";
	
	
	//TABLE_ITEM Columns name
	public static final String KEY_ID_ITEM = "_id";
	public static final String ITEM_NAME = "itemName";
	public static final String ITEM_STATUS = "selectedItem";
		
	//Create TABLE_ITEM statement
	//Item have _id and name
	private static final String CREATE_TABLE_ITEM = "CREATE TABLE " + TABLE_ITEM
			+ " (" + KEY_ID_ITEM + " INTEGER PRIMARY KEY AUTOINCREMENT," + ITEM_NAME + " TEXT not null collate nocase, " + ITEM_STATUS+ " INTEGER not null"+")";
	
	//TABLE_PURCHASE columns name
	public static final String KEY_ID_PURHCASE = "_id";
	public static final String DATE_PURCHASE = "datePurchase";
	public static final String PURCHASE_FROM_ITEM = "purchaseFromItem";
	public static final String PURCHASE_STATUS = "status";
	
	//Create TABLE_PURCHASE statement
	private static final String CREATE_TABLE_PURCHASE = "CREATE TABLE " + TABLE_PURCHASE
			+ "( " + KEY_ID_PURHCASE + " INTEGER PRIMARY KEY AUTOINCREMENT," + DATE_PURCHASE + " DATETIME," + PURCHASE_FROM_ITEM + " INTEGER not null," + PURCHASE_STATUS + " INTEGER" + ")";
	
	
	//Constructor
	public PurchaseDataBase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
			}
			
	

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.i(TAG, "First Create purchaseDataBase");
		db.execSQL(CREATE_TABLE_ITEM);
		db.execSQL(CREATE_TABLE_PURCHASE);
		
	}
	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
