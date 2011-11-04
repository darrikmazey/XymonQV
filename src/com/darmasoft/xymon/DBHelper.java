package com.darmasoft.xymon;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBHelper extends SQLiteOpenHelper {

	static final String TAG = "DBHelper";
	static final String DB_NAME = "xymonqv.db";
	static final int DB_VERSION = 3;
	static final String HOST_TABLE = "hosts";
	static final String STATUS_TABLE = "statuses";
	static final String RUN_TABLE = "runs";
	
	static final String C_ID = BaseColumns._ID;
	static final String C_HOSTNAME = "hostname";
	static final String C_CREATED_AT = "created_at";
	static final String C_HOST_ID = "host_id";
	static final String C_SVC_NAME = "svc_name";
	static final String C_COLOR = "color";
	static final String C_DURATION = "duration";
	static final String C_VERSION = "version";
	
	Context context;
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + HOST_TABLE + " ( " + C_HOSTNAME + " text primary key, " + C_CREATED_AT + " int)";
		db.execSQL(sql);
		sql = "create table " + STATUS_TABLE + " ( " + C_ID + " int primary key, " + C_HOST_ID + " text, " + C_SVC_NAME + " text, " + C_COLOR + " text, " + C_DURATION + " int, " + C_CREATED_AT + " text)"; 
		db.execSQL(sql);
		sql = "create table " + RUN_TABLE + " ( " + C_CREATED_AT + " text primary key, " + C_COLOR + " text, " + C_VERSION + " text)";  
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, String.format("onUpgrade(%d, %d", oldVersion, newVersion));
		db.execSQL("drop table if exists " + HOST_TABLE);
		db.execSQL("drop table if exists " + STATUS_TABLE);
		db.execSQL("drop table if exists " + RUN_TABLE);
		onCreate(db);
	}

	public synchronized boolean delete_all_statuses() {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("delete from statuses");
		return(true);
	}
	
	public synchronized boolean delete_all_hosts() {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("delete from hosts");
		return(true);
	}
	
	public synchronized boolean delete_all_runs() {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("delete from runs");
		return(true);
	}
	public synchronized boolean insert(XymonHost host, Date last_updated) {
		ContentValues values = new ContentValues();
		values.clear();

		SQLiteDatabase db = this.getWritableDatabase();
		
		String last_updated_str = DateHelper.dateToSqlString(last_updated);
   		values.clear();
   		values.put(DBHelper.C_HOSTNAME, host.hostname());
   		values.put(DBHelper.C_CREATED_AT, last_updated_str);
   		try {
			db.insertOrThrow(DBHelper.HOST_TABLE, null, values);
		} catch (SQLiteConstraintException e) {
			// TODO Auto-generated catch block
		}
   	
		db.close();
		
		return(true);
	}
	
	public synchronized boolean insert(XymonService service, Date last_updated) {
		ContentValues values = new ContentValues();
		values.clear();

		SQLiteDatabase db = this.getWritableDatabase();
		String last_updated_str = DateHelper.dateToSqlString(last_updated);
		
		values.put(DBHelper.C_HOST_ID, service.host().hostname());
		values.put(DBHelper.C_SVC_NAME, service.name());
		values.put(DBHelper.C_COLOR, service.color());
		values.put(DBHelper.C_DURATION, service.duration());
		values.put(DBHelper.C_CREATED_AT, last_updated_str);
		try {
			db.insertOrThrow(DBHelper.STATUS_TABLE, null, values);
		} catch (SQLiteConstraintException e) {
			// TODO Auto-generated catch block
		}
		
		db.close();
		
		return(true);
	}
	public synchronized boolean insert_run(Date last_updated, String color, String version) {
		Log.d(TAG, String.format("insert_run(%tF %tT, %s, %s", last_updated, last_updated, color, version));
		
		ContentValues values = new ContentValues();
		values.clear();
		
		SQLiteDatabase db = this.getWritableDatabase();
		String last_updated_str = DateHelper.dateToSqlString(last_updated);
		
		values.put(DBHelper.C_CREATED_AT, last_updated_str);
		values.put(DBHelper.C_COLOR, color);
		values.put(DBHelper.C_VERSION, version);
		
		try {
			db.insertOrThrow(DBHelper.RUN_TABLE, null, values);
		} catch (SQLiteConstraintException e) {
			// ignore duplicate runs
		}
		
		db.close();
		
		return(true);
	}
	
	public XymonQuery load_last_query(XymonServer server) {
		XymonQuery q = new XymonQuery(server);
		
		q.set_hosts(this.load_last_hosts(server));
		q.set_color(this.load_last_color());
		q.set_last_updated(this.load_last_updated());
		q.set_version(this.load_last_version());
		
		return(q);
	}
	public ArrayList<XymonHost> load_last_hosts(XymonServer server) {
		Log.d(TAG, "load_last_hosts()");
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "select distinct(hosts.hostname) from hosts, statuses where statuses.created_at in (select max(created_at) from runs) " +
			"and hosts.hostname = statuses.host_id";
		Cursor cursor = db.rawQuery(sql, null);
		
		ArrayList<XymonHost> hosts = new ArrayList<XymonHost>();
		
		while (cursor.moveToNext()) {
			XymonHost host = new XymonHost(cursor.getString(0));
			load_services_for_host(host);
			host.setServer(server);
			hosts.add(host);
		}
		cursor.close();
		db.close();
		
		return(hosts);
	}
	
	public int load_services_for_host(XymonHost host) {
		Log.d(TAG, "load_services_for_host(" + host.hostname() + ")");
		host.clear_services();
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "SELECT statuses.* from statuses, runs " +
			"WHERE statuses.created_at = runs.created_at " +
			"AND statuses.created_at IN (SELECT max(created_at) FROM runs) " +
			"AND statuses.host_id = '" + host.hostname() + "'";
		Cursor cursor = db.rawQuery(sql, null);
		int count = cursor.getCount();
		
		while (cursor.moveToNext()) {
			String svc_name = cursor.getString(2);
			String svc_color = cursor.getString(3);
			String svc_duration = cursor.getString(4);
			XymonService svc = new XymonService(svc_name, svc_color, false, svc_duration);
			host.add_service(svc);
		}
		cursor.close();
		db.close();
		return(count);
	}
	
	public Date load_last_updated() {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "SELECT " + DBHelper.C_CREATED_AT + " FROM " + DBHelper.RUN_TABLE + " ORDER BY CREATED_AT DESC LIMIT 1";
		Cursor cursor = db.rawQuery(sql, null);
		Date last_updated = null;
		
		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			try {
				last_updated = DateHelper.sqlStringToDate(cursor.getString(0));
			} catch (ParseException e) {
				last_updated = null;
			}
		}
		cursor.close();
		db.close();
		
		return(last_updated);
	}

	public String load_last_color() {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "SELECT " + DBHelper.C_COLOR + " FROM " + DBHelper.RUN_TABLE + " ORDER BY CREATED_AT DESC LIMIT 1";
		Cursor cursor = db.rawQuery(sql, null);
		String color = "black";
		
		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			color = cursor.getString(0);
		}
		cursor.close();
		db.close();
		
		return(color);
	}
	
	public String load_last_version() {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "SELECT " + DBHelper.C_VERSION + " FROM " + DBHelper.RUN_TABLE + " ORDER BY CREATED_AT DESC LIMIT 1";
		Cursor cursor = db.rawQuery(sql, null);
		String version = "unknown";
		
		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			version = cursor.getString(0);
		}
		cursor.close();
		db.close();
		
		return(version);
	}
}
