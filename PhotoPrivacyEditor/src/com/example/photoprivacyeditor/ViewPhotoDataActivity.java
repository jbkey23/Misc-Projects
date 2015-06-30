package com.example.photoprivacyeditor;

import java.io.IOException;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class ViewPhotoDataActivity extends Activity {
	
	private ImageView selectedPhoto = null;
	private TextView photoData = null;
	private TextView imageNameValue = null;
	private TextView dateTakenValue = null;
	private TextView timeTakenValue = null;
	private TextView latitudeValue = null;
	private TextView longitudeValue = null;
	
	
	private ExifInterface curExif = null;
	private ExifInterface newExif = null;
	private String imageName;
	
	private float gpsLat = 999;
	private float gpsLon = 999;
	
	private boolean editDateBool = false;
	private boolean editGPSBool = false;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_photo_data);
		
		Intent intent = this.getIntent();
		String imagePath = intent.getStringExtra("imagePath");
		Bitmap image = null;
		
		imageName = getImageName(imagePath);
		
		selectedPhoto = (ImageView)findViewById(R.id.selectedPhoto_View);
		imageNameValue = (TextView)findViewById(R.id.imageNameValue1);
		dateTakenValue = (TextView)findViewById(R.id.dateTakenValue1);
		timeTakenValue = (TextView)findViewById(R.id.timeTakenValue1);
		latitudeValue = (TextView)findViewById(R.id.latitudeValue1);
		longitudeValue = (TextView)findViewById(R.id.longitudeValue1);

		try {
			curExif = new ExifInterface(imagePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		image = BitmapFactory.decodeFile(imagePath);
		selectedPhoto.setImageBitmap(image);
		
		displayExifData();
	}

	class MyDate
	{
		public String day;
		public String month;
		public String year;
		
		MyDate(String d,String m,String y){day = d; month = m; year = y;}
		MyDate(){day = "NULL"; month = "NULL"; year = "NULL";}
	}
	public String getImageName(String pathName)
	{
		String[] tokens = pathName.split("/");
		return tokens[tokens.length-1];
	}
	
	public MyDate getDate(String timeDate)
	{
		MyDate newDate = new MyDate();
		if (timeDate != null)
		{
			String[] tokens1 = timeDate.split(" ");
			String date = tokens1[0];
			String tokens2[] = date.split(":");
			newDate = new MyDate(tokens2[2],tokens2[1],tokens2[0]);
		}
		
		return newDate;
	}
	
	public String getTime(String timeDate)
	{
		String time = "NULL";
		if (timeDate != null)
		{
			String[] tokens = timeDate.split(" ");
			time = tokens[1];
		}
		return time;
	}
	public void displayExifData()
	{
		String gpsLatitude = "NULL";
		String gpsLongitude = "NULL";
		String dateTime = "NULL";
		String date = "NULL";
		String time = "NULL";
		
		float[] LatLong = new float[2]; 
		if(curExif.getLatLong(LatLong))
		{
			gpsLat = LatLong[0];
			gpsLon = LatLong[1];
			gpsLatitude = Float.toString(gpsLat);
			gpsLongitude = Float.toString(gpsLon);
		}
		
		imageNameValue.setText(imageName);
		dateTime = curExif.getAttribute(ExifInterface.TAG_DATETIME);
		MyDate newDate = getDate(dateTime);
		date = "" + newDate.month + "/" + newDate.day + "/" + newDate.year;
		dateTakenValue.setText(date);
		time = getTime(dateTime);
		timeTakenValue.setText(time);
		
		longitudeValue.setText(gpsLongitude);
		latitudeValue.setText(gpsLatitude);
		
	}
	
	public void viewLocation(View v)
	{
		if(gpsLat != 999 && gpsLon != 999)
		{
			Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + gpsLat + "," + gpsLon + "?q=" + gpsLat + "," + gpsLon +"(You were here)"));
    		
    		if (mapIntent.resolveActivity(this.getPackageManager()) != null)
    		{
    			startActivity(mapIntent);
    		}
    		else
    		{
    			mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=" + gpsLat + "+" + gpsLon));
    			startActivity(mapIntent);
    		}
		}
		else
		{
			Toast.makeText(this, "No GPS Data is avaiable to show!", Toast.LENGTH_SHORT).show();
		}
	}
}
