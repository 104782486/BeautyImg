package com.huewu.pla.sample;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class DownLoadImage extends AsyncTask<String, Void, Bitmap> {  
    PhotoView photoView;  
    HttpURLConnection conn;
  
    public DownLoadImage(PhotoView photoView) {
        this.photoView = photoView;  
    }  
  
    @Override  
    protected Bitmap doInBackground(String... urls) {  
        String url = urls[0];  
        Bitmap tmpBitmap = null;  
        try {
        	URL imageurl = new URL(url);
        	conn = (HttpURLConnection)imageurl.openConnection();//.setConnectTimeout(3000);
        	conn.setConnectTimeout(3000);
            InputStream is = imageurl.openStream();  
            tmpBitmap = BitmapFactory.decodeStream(is);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return tmpBitmap;  
    }  
  
    @Override  
    protected void onPostExecute(Bitmap result) { 
    	if(null==result) {
    		photoView.setImageResource(R.drawable.empty_photo);
    	}else{
        	photoView.setImageBitmap(result); 
    	}
    }  
}  
