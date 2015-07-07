package com.example.photoprivacyeditor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeScreenActivity extends Activity {

	private Cursor cursor;
	
	private int columnIndex;
	private int dataIndex;
	
	private GridView gridview;
	
	private Button viewButton;
	private Button editButton;
	private Button removeButton;
	private Button locateButton;
	private ArrayList<String> imagePaths = new ArrayList<String>();
	private static final String[] imageNumbers = 
			new String[]{"16","27","33","37","54","56","84","85","86",
						"87","88","89","90","91","92","93","94","95",
						"96","97","98","99","100","101","102","103"};
	private static final String[] imagePathsStrings =
			new String[]{"/storage/extSdCard/PhotoPrivacyImages/IMAG0016.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0027.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0033.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0037.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0054.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0056.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0084.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0085.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0086.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0087.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0088.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0089.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0090.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0091.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0092.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0093.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0094.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0095.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0096.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0097.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0098.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0099.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0100.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0101.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0102.jpg",
						 "/storage/extSdCard/PhotoPrivacyImages/IMAG0103.jpg"};
						
	private double startTime;
	private double totalTime;
	private int totalButtonClicks;
	private int viewButtonClicks;
	private int locateButtonClicks;
	private int editButtonClicks;
	private int removeButtonClicks;
	private int onlyGPSButtonClicks;
	private int onlyTimeButtonClicks;
	private int removeBothButtonClicks;
	private int imageClicks;
	
	private static final int REMOVE_GPS_DATA = 0;
	private static final int REMOVE_TIME_DATE_DATA = 1;
	private static final int REMOVE_BOTH = 2;
	
	private ExifInterface curExif = null;
	private ExifInterface newExif = null;
	private ImageAdapter ia = null;
	private MultiChoiceModeListener mcml = null;
	private int taskNum;
	private static final String LOG_HEADS = "Task Number;Total Time;Total Button Clicks;View Button Clicks;Locate Button Clicks;Edit Button Clicks;Remove Button Clicks;Only GPS Button Clicks;Only Time Button Clicks;Remove Both Button Clicks;Image Clicks";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);
		
	    viewButton = (Button)findViewById(R.id.viewButton);
	    editButton = (Button)findViewById(R.id.editButton);
	    removeButton = (Button)findViewById(R.id.removeButton);
	    locateButton = (Button)findViewById(R.id.locateButton);
	    ia = new ImageAdapter(this);
	    mcml = new MultiChoiceModeListener();
	    taskNum = 1;
	    
	    setUpGridView();
	}
	
	public void setUpGridView()
	{
	    String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
	   

	    cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
	    									projection, 
	    									MediaStore.Images.Media.DATA + " like ? ",
	    									new String[] {"%/storage/extSdCard/PhotoPrivacyImagesNum/%"},
	    									null);
	    
	    columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
	    dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    
	    gridview = (GridView) findViewById(R.id.gridview);
	    gridview.setAdapter(ia);
	    gridview.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
	    gridview.setMultiChoiceModeListener(mcml);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		setUpGridView();
	}

	 /**
     * Adapter for our image files.
     */
    private class ImageAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater mLayoutInflater;

        public ImageAdapter(Context localContext) {
            context = localContext;
            mLayoutInflater = LayoutInflater.from(localContext);
        }

        public int getCount() {
            return cursor.getCount();
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
        	CheckableLayout l;
        	ImageView picturesView;
        	//ViewHolder mVHolder;
        	cursor.moveToPosition(position);
        	int imageID = cursor.getInt(columnIndex);
        	String imagePath = cursor.getString(dataIndex);

        	if(convertView == null)
        	{
        		//convertView = mLayoutInflater.inflate(R.layout.customgrid,parent,false);
        		//mVHolder = new ViewHolder();
        		//mVHolder.mImageView = (ImageView)convertView.findViewById(R.id.imgview);
        		//mVHolder.mTextView = (TextView)convertView.findViewById(R.id.text);
        		//mVHolder.mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        		//mVHolder.mImageView.setLayoutParams(new GridView.LayoutParams(350,350));
        		//mVHolder.mTextView.setPadding(8, 8, 8, 8);
        		//convertView.setTag(mVHolder);
	        	picturesView = new ImageView(context);
	        	picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	        	picturesView.setPadding(8, 8, 8, 8);
	        	picturesView.setLayoutParams(new GridView.LayoutParams(350, 350));
	       
	        	l = new CheckableLayout(context);
	        	l.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT,
	        												GridView.LayoutParams.MATCH_PARENT));
	        	l.addView(picturesView);
        	}
        	else
        	{
        		l = (CheckableLayout) convertView;
        		//mVHolder = (ViewHolder)convertView.getTag();
        		
        		picturesView = (ImageView) l.getChildAt(0);
        		
        	}
        	
        	
        	//picturesView.setImageURI(Uri.withAppendedPath(
            //MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imageID));
        	
        	picturesView.setImageBitmap(decodeSampledBitmapFromFile(imagePath,100,100));
        	//picturesView.setBackground(getResources().getDrawable(R.drawable.sixteen));
        	
        	//mVHolder.mImageView.setImageBitmap(decodeSampledBitmapFromFile(imagePath,100,100));
        	//mVHolder.mTextView.setText(imageNumbers[position]);
        	
        	return l;
        }
    }
    
    
//    static class ViewHolder
//    {
//    	ImageView mImageView;
//    	TextView mTextView;
//    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
    
    
    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }
    
    public class CheckableLayout extends FrameLayout implements Checkable {
        private boolean mChecked;
 
        public CheckableLayout(Context context) {
            super(context);
        }
 
        @SuppressWarnings("deprecation")
        public void setChecked(boolean checked) {
            mChecked = checked;
            setBackgroundDrawable(checked ? getResources().getDrawable(
                    R.drawable.blue) : null);
        }
 
        public boolean isChecked() {
            return mChecked;
        }
 
        public void toggle() {
            setChecked(!mChecked);
        }
 
    }

		    public class MultiChoiceModeListener implements
		    GridView.MultiChoiceModeListener {
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		    mode.setTitle("Select Items");
		    mode.setSubtitle("One item selected");
		    viewButton.setEnabled(true);
		    locateButton.setEnabled(true);
	        editButton.setEnabled(true);
	        removeButton.setEnabled(true);
		    return true;
		}
		
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		    return true;
		}
		
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		    return true;
		}
		
		public void onDestroyActionMode(ActionMode mode) {
			removeButton.setEnabled(false);
	        viewButton.setEnabled(false);
	        locateButton.setEnabled(false);
	        editButton.setEnabled(false);
	        imagePaths.clear();
		}
		
		public void onItemCheckedStateChanged(ActionMode mode, int position,
		        long id, boolean checked) {
		    int selectCount = gridview.getCheckedItemCount();
		    cursor.moveToPosition(position);
		    //String path = cursor.getString(dataIndex);
		    String path = imagePathsStrings[position];
		    imageClicks++;
		    
		    switch (selectCount) {
		    case 1:
		        mode.setSubtitle("One item selected");
		        viewButton.setEnabled(true);
		        locateButton.setEnabled(true);
		        editButton.setEnabled(true);
		        removeButton.setEnabled(true);
		        if(checked)
		        {
		        	imagePaths.add(path);
		        	Log.d("Selected Item Checked", "Added path to list" + path);
		        	//gridview.getChildAt(position).setBackground(getResources().getDrawable(
		            //        R.drawable.blue));
		        }
		        else
		        {
		        	imagePaths.remove(path);
		        	Log.d("Selected Item Unchecked", "Removed path from list" + path);
		        	//gridview.getChildAt(position).setBackground(getResources().getDrawable(
		            //        R.drawable.white));
		        }
		        Log.d("Selected Photo Path", path);
		        break;
		    default:
		        mode.setSubtitle("" + selectCount + " items selected");
		        removeButton.setEnabled(true);
		        viewButton.setEnabled(false);
		        locateButton.setEnabled(false);
		        editButton.setEnabled(false);
		        
		        if(checked)
		        {
		        	imagePaths.add(path);
		        	Log.d("Selected Item Checked", "Added path to list" + path);
		        	//gridview.getChildAt(position).setBackground(getResources().getDrawable(
		             //       R.drawable.blue));
		        }
		        else
		        {
		        	imagePaths.remove(path);
		        	Log.d("Selected Item Unchecked", "Removed path from list" + path);
		        	//gridview.getChildAt(position).setBackground(getResources().getDrawable(
		              //      R.drawable.white));
		        }
		        Log.d("Selected Photo Path", path);
		        break;
		    }
		}
}
	public void viewPhotoData(View v)
	{
		viewButtonClicks++;
		Intent intent = new Intent(this, ViewPhotoDataActivity.class);
		intent.putExtra("imagePath", imagePaths.get(0));
		startActivity(intent);
	}
	
	public void editPhotoData(View v)
	{
		editButtonClicks++;
		Intent intent = new Intent(this, EditPhotoDataActivity.class);
		intent.putExtra("imagePath", imagePaths.get(0));
		startActivity(intent);
	}
	
	public void locateImage(View v)
	{
		locateButtonClicks++;
		float gpsLat = 999;
		float gpsLon = 999;
		
		
		try {
			curExif = new ExifInterface(imagePaths.get(0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		float[] LatLong = new float[2]; 
		if(curExif.getLatLong(LatLong))
		{
			gpsLat = LatLong[0];
			gpsLon = LatLong[1];
		}
		
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
			Toast.makeText(this, "Cannot locate! No GPS data available!", Toast.LENGTH_SHORT).show();
		}
		
	}
	@SuppressWarnings("deprecation")
	public void removePhotoData(View v)
	{
		removeButtonClicks++;
		String[] choices = {"Remove GPS Data", "Remove Time/Date Data", "Remove Both"};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Choose which data to remove:");
		
		builder.setItems(choices, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == REMOVE_GPS_DATA)
				{
					onlyGPSButtonClicks++;
					removeGPS();
				}
				else if(which == REMOVE_TIME_DATE_DATA)
				{
					onlyTimeButtonClicks++;
					removeTimeDate();
				}
				else if(which == REMOVE_BOTH)
				{
					removeBothButtonClicks++;
					removeBoth();
				}
			}
		});
		
		builder.create();
		builder.show();
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
	
	public void removeGPS()
	{
		for(int i = 0; i<imagePaths.size(); i++)
		{
			if(imagePaths.get(i)!= null)
			{
				String imageName = getImageName(imagePaths.get(i));
				
				String fileName = "noGPS_" + imageName;
				String dstPath = "/storage/extSdCard/ChangedPhotoPrivacyImages/" + fileName;
				File src = new File(imagePaths.get(i));
				File dst = new File(dstPath);
				
				try {
					copy(src,dst);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
//				try {
//					newExif = new ExifInterface(dstPath);
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
//				//newExif.setAttribute(ExifInterface.TAG_APERTURE, curExif.getAttribute(ExifInterface.TAG_APERTURE));
//				//newExif.setAttribute(ExifInterface.TAG_DATETIME, curExif.getAttribute(ExifInterface.TAG_DATETIME));
//				//newExif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, curExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
//				//newExif.setAttribute(ExifInterface.TAG_FLASH, curExif.getAttribute(ExifInterface.TAG_FLASH));
//				//newExif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, curExif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
//				newExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, null);
//				//newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, curExif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
//				//newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, curExif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
//				//newExif.setAttribute(ExifInterface.TAG_ISO, curExif.getAttribute(ExifInterface.TAG_ISO));
//				//newExif.setAttribute(ExifInterface.TAG_MAKE, curExif.getAttribute(ExifInterface.TAG_MAKE));
//				//newExif.setAttribute(ExifInterface.TAG_MODEL, curExif.getAttribute(ExifInterface.TAG_MODEL));
//				//newExif.setAttribute(ExifInterface.TAG_ORIENTATION, curExif.getAttribute(ExifInterface.TAG_ORIENTATION));
//				//newExif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, curExif.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
//				//newExif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, curExif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP));
//				
//				try {
//					newExif.saveAttributes();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}
		

		
		Toast.makeText(this, "GPS Data Removed Successfully!", Toast.LENGTH_SHORT).show();
				
	}
	
	public void removeTimeDate()
	{
		for(int i = 0; i< imagePaths.size(); i++)
		{
			if(imagePaths.get(i) != null)
			{
				String imageName = getImageName(imagePaths.get(i));
				
				String fileName = "noTimeDate_" + imageName;
				String dstPath = "/storage/extSdCard/ChangedPhotoPrivacyImages/" + fileName;
				File src = new File(imagePaths.get(i));
				File dst = new File(dstPath);
				
				try {
					copy(src,dst);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
//				try {
//					newExif = new ExifInterface(dstPath);
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
//				//newExif.setAttribute(ExifInterface.TAG_APERTURE, curExif.getAttribute(ExifInterface.TAG_APERTURE));
//				newExif.setAttribute(ExifInterface.TAG_DATETIME, null);
//				//newExif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, curExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
//				//newExif.setAttribute(ExifInterface.TAG_FLASH, curExif.getAttribute(ExifInterface.TAG_FLASH));
//				//newExif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, curExif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
//				//newExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null);
//				//newExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null);
//				//newExif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, null);
//				//newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null);
//				//newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null);
//				//newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null);
//				//newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null);
//				//newExif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, null);
//				//newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, curExif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
//				//newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, curExif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
//				//newExif.setAttribute(ExifInterface.TAG_ISO, curExif.getAttribute(ExifInterface.TAG_ISO));
//				//newExif.setAttribute(ExifInterface.TAG_MAKE, curExif.getAttribute(ExifInterface.TAG_MAKE));
//				//newExif.setAttribute(ExifInterface.TAG_MODEL, curExif.getAttribute(ExifInterface.TAG_MODEL));
//				//newExif.setAttribute(ExifInterface.TAG_ORIENTATION, curExif.getAttribute(ExifInterface.TAG_ORIENTATION));
//				//newExif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, curExif.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
//				//newExif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, curExif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP));
//				
//				try {
//					newExif.saveAttributes();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}	
		Toast.makeText(this, "Time/Date Data Removed Successfully!", Toast.LENGTH_SHORT).show();
	}
	
	public void removeBoth()
	{
		for(int i = 0; i < imagePaths.size(); i++)
		{
			if(imagePaths.get(i) != null)
			{
				String imageName = getImageName(imagePaths.get(i));
				
				String fileName = "noGPSnoTimeDate_" + imageName;
				String dstPath = "/storage/extSdCard/ChangedPhotoPrivacyImages/" + fileName;
				File src = new File(imagePaths.get(i));
				File dst = new File(dstPath);
				
				try {
					copy(src,dst);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
//				try {
//					newExif = new ExifInterface(dstPath);
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
//				//newExif.setAttribute(ExifInterface.TAG_APERTURE, curExif.getAttribute(ExifInterface.TAG_APERTURE));
//				newExif.setAttribute(ExifInterface.TAG_DATETIME, null);
//				//newExif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, curExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
//				//newExif.setAttribute(ExifInterface.TAG_FLASH, curExif.getAttribute(ExifInterface.TAG_FLASH));
//				//newExif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, curExif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
//				newExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null);
//				newExif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, null);
//				//newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, curExif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
//				//newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, curExif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
//				//newExif.setAttribute(ExifInterface.TAG_ISO, curExif.getAttribute(ExifInterface.TAG_ISO));
//				//newExif.setAttribute(ExifInterface.TAG_MAKE, curExif.getAttribute(ExifInterface.TAG_MAKE));
//				//newExif.setAttribute(ExifInterface.TAG_MODEL, curExif.getAttribute(ExifInterface.TAG_MODEL));
//				//newExif.setAttribute(ExifInterface.TAG_ORIENTATION, curExif.getAttribute(ExifInterface.TAG_ORIENTATION));
//				//newExif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, curExif.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
//				//newExif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, curExif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP));
//				
//				try {
//					newExif.saveAttributes();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}	
		Toast.makeText(this, "Both Data Sets Removed Successfully!", Toast.LENGTH_SHORT).show();
	}
	
	public String getImageName(String pathName)
	{
		String[] tokens = pathName.split("/");
		return tokens[tokens.length-1];
	}
	
	public void appendLog(String text)
	{       
	   File logFile = new File("/storage/extSdCard/Logs/logFile.txt");
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
	
	public void startTask(View v)
	{
		startTime = SystemClock.elapsedRealtime();
		Toast.makeText(this, "Timer Started!", Toast.LENGTH_SHORT).show();
	}
	
	public void finishTask(View v)
	{
		totalTime = SystemClock.elapsedRealtime() - startTime;
		totalTime = totalTime/1000.0;
		totalButtonClicks = viewButtonClicks+locateButtonClicks+locateButtonClicks+editButtonClicks+
							removeButtonClicks+onlyGPSButtonClicks+onlyTimeButtonClicks+removeBothButtonClicks+imageClicks;
		String log = taskNum + ";" + Double.toString(totalTime) + ";" + totalButtonClicks + ";" + viewButtonClicks 
					+ ";" + locateButtonClicks + ";" + editButtonClicks + ";" + removeButtonClicks + ";" + onlyGPSButtonClicks
					+ ";" + onlyTimeButtonClicks + ";" + removeBothButtonClicks + ";" + imageClicks;
		if(taskNum == 1)
		{
			appendLog(LOG_HEADS);
		}
		appendLog(log);
		
		taskNum++;
		totalButtonClicks = 0;
		viewButtonClicks = 0;
		locateButtonClicks = 0;
		editButtonClicks = 0;
		removeButtonClicks = 0;
		onlyGPSButtonClicks = 0;
		onlyTimeButtonClicks = 0;
		removeBothButtonClicks = 0;
		imageClicks = 0;
		Toast.makeText(this,"Total Time: " + totalTime, Toast.LENGTH_SHORT).show();
	}

}
