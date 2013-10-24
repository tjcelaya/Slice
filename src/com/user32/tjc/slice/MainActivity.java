package com.user32.tjc.slice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.HttpMultipartMode; 
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.MimeTypeMap;
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
	private HttpClient httpc;
	protected static ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		Intent i = getIntent();
		if (i != null) {
			Uri p = i.getParcelableExtra(Intent.EXTRA_STREAM);
			if (p != null) {
				new UploadFile().execute(p);
				
			}
		}
		
		mediaContentUri = MediaStore.Files.getContentUri("external");
		LM = getLoaderManager();
		httpc = new DefaultHttpClient();

		
		mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, 
				null, 
				new String[] {MediaStore.Files.FileColumns.DISPLAY_NAME},
				new int[] {android.R.id.text1},
				0);  //flags not used with loadermanager

		setListAdapter(mAdapter);
		LM.initLoader(1, null, this);
		
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		new UploadFile().execute(Uri.parse(mediaContentUri+"/"+id));
	}
	
	private class UploadFile extends AsyncTask<Uri, Integer, String> {

        private static final String UPLOAD_URL = "http://ww2.cs.fsu.edu/~celaya/upload.php";
        private static final String SLICE_URL = "http://ww2.cs.fsu.edu/~celaya/slice/";

        @Override
        protected void onPreExecute() {
        	MainActivity.pd = new ProgressDialog(MainActivity.this);
			pd.setTitle("Uploading...");
			pd.setMessage("Pls wait.");
			pd.setCancelable(false);
			pd.setIndeterminate(true);
			pd.show();
        }

		@SuppressWarnings("deprecation")
		@Override
        protected String doInBackground(Uri... contentUri) {

			Random r = new Random();
    		//TODO file upload goes here
    	    HttpClient httpclient = new DefaultHttpClient();

    	    HttpPost httppost = new HttpPost(UPLOAD_URL);

    	    HttpResponse response = null;
    	    String uploadedUrl = "";
    	    try {
				MultipartEntityBuilder entity = MultipartEntityBuilder.create();
//				entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//				httppost.addHeader("Content-Type", getContentResolver().getType(contentUri[0]));
				
				uploadedUrl = Integer.toHexString(r.hashCode());
						
				entity.addPart("file", 
						new InputStreamBody(
								getContentResolver().openInputStream(contentUri[0]),
								uploadedUrl
								)
				);
				
				Cursor cursor = getContentResolver().query(contentUri[0], new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                cursor.moveToFirst();   
                entity.addPart("fullpath", 
                		new StringBody(
                				cursor.getString(0)
                				)
                );
                entity.addPart("filetype", 
                		new StringBody(
                				cursor.getString(0).replaceFirst("(.*)(\\.)(\\w+)$", "$2$3")
                				)
                );
                
                uploadedUrl += cursor.getString(0).replaceFirst("(.*)(\\.)(\\w+)$", "$2$3");
                		
				cursor.close();

				httppost.setEntity(entity.build());
				response = httpclient.execute(httppost);
    	    } catch (Exception e) {
    	    	e.printStackTrace();
    	    	Log.e(MainActivity.LOG_TAG, "error :(");
    	    	uploadedUrl = "";
    	    }
    	    
    	    if (response != null) {
    	    	Log.w(MainActivity.LOG_TAG, response.getStatusLine().getReasonPhrase());
    	    	try {
					if (EntityUtils.toString(response.getEntity()).contains("Received"))
	    	    		return SLICE_URL + uploadedUrl;

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
    	    
    	    return SLICE_URL;
    	    	
        }

        @Override
        protected void onProgressUpdate(Integer... p) {}

        @Override
        protected void onPostExecute(String result) {
        	if (MainActivity.pd != null) {
        		pd.dismiss();
        		if (result.equals(SLICE_URL)) {
        			AlertDialog.Builder dBuilder = new AlertDialog.Builder(MainActivity.this);
        	 
        				// set title
        			dBuilder.setTitle("Uh Oh");
        				// set dialog message
        			dBuilder.setMessage("There was a problem with your slice, your file may be too bPress Back to close.");
        			dBuilder.create().show();
        		} else {
		        	Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		        	sendIntent.setType("vnd.android-dir/mms-sms");
		        	sendIntent.putExtra("address", "");
		        	sendIntent.putExtra("sms_body", result);
		        	startActivity(sendIntent);
        		}
        	}
        }
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
				MediaStore.Files.FileColumns.DATE_ADDED+" DESC");
		
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
