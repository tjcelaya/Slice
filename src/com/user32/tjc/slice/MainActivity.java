package com.user32.tjc.slice;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends ListActivity implements LoaderCallbacks<Cursor> {
	public static final String LOG_TAG = "slice";
	private static LoaderManager LM;
	private static CursorLoader CurLoader;
	private static SimpleCursorAdapter mAdapter;
	private Uri mediaContentUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mediaContentUri = MediaStore.Files.getContentUri("external");
		LM = getLoaderManager();

		
		mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, 
				null, 
				new String[] {MediaStore.Files.FileColumns.DISPLAY_NAME},
				new int[] {android.R.id.text1},
				0);  //flags not used with loadermanager
//		
//		for (PackageInfo pack : getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS)) {
//	        ProviderInfo[] providers = pack.providers;
//	        if (providers != null) {
//	            for (ProviderInfo provider : providers) {
//	                Log.d(LOG_TAG, "provider: " + provider.authority);
//	            }
//	        }
//	    }
//		

		setListAdapter(mAdapter);
		LM.initLoader(1, null, this);
		
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Toast.makeText(this, mediaContentUri+"/"+id, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME };
		
		/**
		* This requires the URI of the Content Provider
		* projection is the list of columns of the database to return. Null will return all the columns
		* selection is the filter which declares which rows to return. Null will return all the rows for the given URI.
		* selectionArgs:  You may include ?s in the selection, which will be replaced
		* by the values from selectionArgs, in the order that they appear in the selection. 
		* The values will be bound as Strings.
		* sortOrder determines the order of rows. Passing null will use the default sort order, which may be unordered.
		* To back a ListView with a Cursor, the cursor must contain a column named _ID.
		*/
		 
		CurLoader = new CursorLoader(this, 
				mediaContentUri, 
				projection, 
				MediaStore.Files.FileColumns.MEDIA_TYPE+" != 0",
				null, 
				null);
		
		return CurLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		if(mAdapter!=null && c!=null)
			mAdapter.swapCursor(c); //swap the new cursor in.
		else
			Log.v(LOG_TAG,"OnLoadFinished: mAdapter is null");
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		if(mAdapter!=null)
			mAdapter.swapCursor(null);
		else
			Log.v(LOG_TAG,"OnLoadFinished: mAdapter is null");		
	}

}
