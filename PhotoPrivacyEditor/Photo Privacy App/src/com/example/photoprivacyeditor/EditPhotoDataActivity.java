package com.example.photoprivacyeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.photoprivacyeditor.ViewPhotoDataActivity.MyDate;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class EditPhotoDataActivity extends Activity {

	private ImageView selectedPhoto = null;
	private TextView photoData = null;
	private TextView imageNameValue = null;
	private EditText dateTakenValue = null;
	private EditText timeTakenValue = null;
	private EditText latitudeValue = null;
	private EditText longitudeValue = null;
	
	
	private ExifInterface curExif = null;
	private ExifInterface newExif = null;
	private String imageName;
	
	private float gpsLat = 999;
	private float gpsLon = 999;
	
	private String imagePath;
	
	private int day;
	private int month;
	private int year;
	
	private int hour;
	private int min;
	
	private final int DATE_PICKER_ID = 0;
	private final int TIME_PICKER_ID = 1;
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_photo_data);
		
		Intent intent = this.getIntent();
		imagePath = intent.getStringExtra("imagePath");
		Bitmap image = null;
		
		imageName = getImageName(imagePath);
		
		selectedPhoto = (ImageView)findViewById(R.id.selectedPhoto_Edit);
		imageNameValue = (TextView)findViewById(R.id.imageNameValue);
		dateTakenValue = (EditText)findViewById(R.id.dateTakenValue);
		timeTakenValue = (EditText)findViewById(R.id.timeTakenValue);
		latitudeValue = (EditText)findViewById(R.id.latitudeValue);
		longitudeValue = (EditText)findViewById(R.id.longitudeValue);

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

	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		switch(id)
		{
		case DATE_PICKER_ID:
			return new DatePickerDialog(this, datePickerListener, year, month, day);
		case TIME_PICKER_ID:
			return new TimePickerDialog(this, timePickerListener, hour, min, false);
		default:
			return null;
		}
	}
	
	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			dateTakenValue.setText((selectedMonth+1) + "/" + selectedDay + "/"
					+ selectedYear);
		}
	};
	
	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			int hour;
			String am_pm;
			if (hourOfDay > 12) {
				hour = hourOfDay - 12;
				am_pm = "PM";
			} else {
				hour = hourOfDay;
				am_pm = "AM";
			}
			timeTakenValue.setText(hour + " : " + minute + " " + am_pm);
		}
	};
	
	public String getImageName(String pathName)
	{
		String[] tokens = pathName.split("/");
		return tokens[tokens.length-1];
	}
	
	class MyDate
	{
		public String day;
		public String month;
		public String year;
		
		MyDate(String d,String m,String y){day = d; month = m; year = y;}
		MyDate(){day = "NULL"; month = "NULL"; year = "NULL";}
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
			String[] tokens2 = time.split(":");
			hour = Integer.parseInt(tokens2[0]);
			min = Integer.parseInt(tokens2[1]);
			String min2 = tokens2[1];
			
			if(hour > 11)
			{
				if(hour > 12)
				{
					hour = hour - 12;
					if(hour == 12)
					{
						time = "" + hour + ":" + min2 + " " + "AM";
					}
					else
					{
						time = "" + hour + ":" + min2 + " " + "PM";
					}
					hour = hour + 12;
				}
				else
				{
					time = "" + hour + ":" + min2 + " " + "PM";
				}
				
			}
			else
			{
				time = "" + hour + ":" + min2 + " " + "AM";			}
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
		day = Integer.parseInt(newDate.day);
		month = Integer.parseInt(newDate.month)-1;
		year = Integer.parseInt(newDate.year);
		dateTakenValue.setText(date);
		time = getTime(dateTime);
		
		timeTakenValue.setText(time);
		
		longitudeValue.setText(gpsLongitude);
		latitudeValue.setText(gpsLatitude);
		
	}
	
	@SuppressWarnings("deprecation")
	public void chooseDate(View v)
	{
		showDialog(DATE_PICKER_ID);
	}
	
	@SuppressWarnings("deprecation")
	public void chooseTime(View v)
	{
		showDialog(TIME_PICKER_ID);
	}
	
	public void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
	
	public void saveChanges(View v)
	{
		String fileName = "edit_" + imageName;
		String dstPath = "/storage/extSdCard/ChangedPhotoPrivacyImages/" + fileName;
		File src = new File(imagePath);
		File dst = new File(dstPath);
		
		try {
			copy(src,dst);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		try {
			newExif = new ExifInterface(dstPath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//newExif.setAttribute(ExifInterface.TAG_APERTURE, curExif.getAttribute(ExifInterface.TAG_APERTURE));
		//newExif.setAttribute(ExifInterface.TAG_DATETIME, curExif.getAttribute(ExifInterface.TAG_DATETIME));
		//newExif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, curExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
		//newExif.setAttribute(ExifInterface.TAG_FLASH, curExif.getAttribute(ExifInterface.TAG_FLASH));
		//newExif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, curExif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
		//newExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null);
		//newExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null);
		//newExif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, null);
		//newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null);
		//newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null);
		//newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null);
		//newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null);
		//newExif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, null);
		//newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, curExif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
		//newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, curExif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
		//newExif.setAttribute(ExifInterface.TAG_ISO, curExif.getAttribute(ExifInterface.TAG_ISO));
		//newExif.setAttribute(ExifInterface.TAG_MAKE, curExif.getAttribute(ExifInterface.TAG_MAKE));
		//newExif.setAttribute(ExifInterface.TAG_MODEL, curExif.getAttribute(ExifInterface.TAG_MODEL));
		//newExif.setAttribute(ExifInterface.TAG_ORIENTATION, curExif.getAttribute(ExifInterface.TAG_ORIENTATION));
		//newExif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, curExif.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
		//newExif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, curExif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP));
		
		try {
			newExif.saveAttributes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Toast.makeText(this, "Data Edit Successful!", Toast.LENGTH_SHORT).show();
		finish();
	}
}
