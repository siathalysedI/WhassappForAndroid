package com.jheto.xekri.api.whatssap;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

/*
 * Author: Jheto Xekri
 * eMail: jheto.xekri@outlook.com
 * Web: http://about.me/jheto.xekri
 * */
public final class Whatssapp {

	private Whatssapp() {}
 
    //*****************************************************
    
    private final static String WHATSSAP_APP_ID = "com.whatsapp";
    private final static boolean DEBUG = false;
    
    public final static int IMAGE_TYPE = 0;
	public final static int AUDIO_TYPE = 1;
	public final static int VIDEO_TYPE = 2;
	
	public final static void shareTextWithContact(Context context, String text){
		if(text == null || text.length() == 0) text = "";
		try{
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.setPackage(WHATSSAP_APP_ID);
			intent.putExtra(Intent.EXTRA_TEXT, text);
			context.startActivity(Intent.createChooser(intent, ""));
		}catch(Exception e){
			if(DEBUG) Log.e("Exception", e.toString());
		}
	}
	
	public final static void shareMediaWithContact(Context context, Uri uri, int mediaType){
		try{
			if(mediaType >= IMAGE_TYPE && mediaType <= VIDEO_TYPE && uri != null){
				Intent intent = new Intent(Intent.ACTION_SEND);
				if(mediaType == IMAGE_TYPE) intent.setType("image/*");
				else if(mediaType == AUDIO_TYPE) intent.setType("audio/*");
				else if(mediaType == VIDEO_TYPE) intent.setType("video/*");
				intent.setPackage(WHATSSAP_APP_ID);
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				context.startActivity(Intent.createChooser(intent, ""));
			}	
		}catch(Exception e){
			if(DEBUG) Log.e("Exception", e.toString());
		}
	}
	
	public final static void openChatWithPhone(Context context, String phoneNumber) {
		try{
			Uri uri = Uri.parse("smsto:" + phoneNumber);
			Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
			intent.setPackage(WHATSSAP_APP_ID);  
			context.startActivity(intent);
		}catch(Exception e){
			if(DEBUG) Log.e("Exception", e.toString());
		}
	}
	
	public final static void launchMarket(Context context){
		try{
			Intent intent = new Intent(Intent.ACTION_VIEW, 
			Uri.parse("market://details?id=" + WHATSSAP_APP_ID));
			context.startActivity(intent);
		}catch(Exception e){
			if(DEBUG) Log.e("Exception", e.toString());
		}
	}
	
	public final static boolean isInstalled(Context context){
		Boolean installed = false;
		try{
			PackageManager mPm = context.getPackageManager();
			PackageInfo info = mPm.getPackageInfo(WHATSSAP_APP_ID, 0);
			installed = info != null;
		}catch(Exception e){
			installed = false;
			if(DEBUG) Log.e("Exception", e.toString());
		}
		return installed;
	}
	
	public final static void createPhoneNumber(Context context, String displayName, String mobileNumber, String email){
		try{
			if(displayName.length()>0 && mobileNumber.length()>0){
				
				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build());
                
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                        .build());
                
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
                        .build());
                
                if(email != null && email.length()>0) {
                     ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    	.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .build());
                }
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
			}
		}catch(Exception e){
			if(DEBUG) Log.e("Exception", e.toString());
		}	
	}
	
	public final static boolean existPhoneNumber(Context context, String phoneNumber){
		boolean exist = false;
		
		try{
			ContentResolver contentResolver = context.getContentResolver();
			Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					String contact_id = cursor.getString(cursor.getColumnIndex( ContactsContract.Contacts._ID ));
					int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER )));
					if (hasPhoneNumber > 0) {
						Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
								null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { contact_id }, null);
						while (phoneCursor.moveToNext()) {
							String phoneNumberContact = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							if(phoneNumber.equals(phoneNumberContact)){
								exist = true;
								phoneCursor.close();
								cursor.close();
								return exist;
							}
						}
						phoneCursor.close();
					}
				}
			}
			cursor.close();
		}catch(Exception e){
			exist = false;
			if(DEBUG) Log.e("Exception", e.toString());
		}
		
		return exist;
	}
	
}
