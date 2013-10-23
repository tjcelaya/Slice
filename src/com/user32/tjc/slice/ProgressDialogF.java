package com.user32.tjc.slice;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class ProgressDialogF extends DialogFragment {

	Context cxt;
    
	public ProgressDialogF() {
        cxt = getActivity();
    }
    
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(cxt)
	        .setTitle("Uploading")
	        .setMessage("It's happening")
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.dismiss();
	            }
        	});


        return alertDialogBuilder.create();
    }
}
