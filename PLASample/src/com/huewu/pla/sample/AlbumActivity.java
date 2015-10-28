package com.huewu.pla.sample;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "ClickableViewAccessibility" })
public class AlbumActivity extends Activity {

	private Handler handler;
	private String albumUrl = null;
	private String htmlContent = null;
	private int totalpage = 0;
	private List<String> imageUrls;
	private int totalimage = 0;
	int startX = 0;
	int endX = 0;
	int current = 0;
	private ViewPager mViewPager;
	private PhotoView photoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewPager = new HackyViewPager(this);
		setContentView(mViewPager);
		Intent intent = getIntent();
		albumUrl = intent.getStringExtra("albumUrl");
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0x123) {
					if (htmlContent.contains("arcmain")) {
						if (htmlContent.contains("article_page")) {
							int pagestart = htmlContent.indexOf("/",
									htmlContent.indexOf("article_page")) + 1;
							int pageend = htmlContent.indexOf("<", pagestart);
							String pages = htmlContent.substring(pagestart,
									pageend);
							totalpage = Integer.parseInt(pages);
						}
						htmlContent = htmlContent.substring(
								htmlContent.indexOf("arcmain"),
								htmlContent.indexOf("</div>",
										htmlContent.indexOf("arcmain")));
						int imagestart = htmlContent.indexOf("http://");
						int imageend = htmlContent.indexOf(".jpg") + 4;
						String imageurl = htmlContent.substring(imagestart,
								imageend) + "!720.jpg";
						imageUrls = new ArrayList<String>();
						totalimage = totalpage * 2 - 1;
						for (int i = 1; i < totalimage; i++) {
							String next = (imageurl.substring(0,
									imageurl.indexOf(".jpg") - 1))
									+ i + ".jpg!720.jpg";
							imageUrls.add(next);
						}
					}
				}
				mViewPager.setAdapter(new SamplePagerAdapter(imageUrls));

			}
		};
		new Thread() {
			public void run() {
				GetHtmlSourceCode();
				Message msg = new Message();
				msg.what = 0x123;
				AlbumActivity.this.handler.sendEmptyMessage(0x123);
			}
		}.start();

	}

	class SamplePagerAdapter extends PagerAdapter {

		private List<String> imageUrls;

		public SamplePagerAdapter(List<String> imageUrls) {
			super();
			this.imageUrls = imageUrls;
		}

		@Override
		public int getCount() {
			return imageUrls.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			photoView = new PhotoView(container.getContext());
			new DownLoadImage(photoView).execute(new String[] { imageUrls.get(position) });
			Log.i("zhangx","POSITION:"+position+";当前显示图片："+imageUrls.get(position));
			container.addView(photoView, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			// 长按弹出保存
			photoView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							AlbumActivity.this);
					builder.setItems(
							new String[] { getResources().getString(
									R.string.save_picture) },
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									photoView.setDrawingCacheEnabled(true);
									Bitmap imageBitmap = photoView.getDrawingCache();
									if (imageBitmap != null) {
										new SaveImageTask()
												.execute(imageBitmap);
									}
								}
							});
					builder.show();
					return true;
				}
			});
			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

	private class SaveImageTask extends AsyncTask<Bitmap, Void, String> {
		@Override
		protected String doInBackground(Bitmap... params) {
			String result = getResources().getString(
					R.string.save_picture_failed);
			try {
				String sdcard = Environment.getExternalStorageDirectory()
						.toString();

				File file = new File(sdcard + "/Xiang");
				if (!file.exists()) {
					file.mkdirs();
				}
				File imageFile = new File(file.getAbsolutePath(),
						new Date().getTime() + ".jpg");
				FileOutputStream outStream = new FileOutputStream(imageFile);
				Bitmap image = params[0];
				image.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
				outStream.flush();
				outStream.close();
				result = getResources().getString(
						R.string.save_picture_success, file.getAbsolutePath());
				photoView.setDrawingCacheEnabled(false);
			} catch (Exception e) {
				e.printStackTrace();
				result = "保存失败，请重试或检查SD卡！";
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT)
					.show();
		}
	}

	protected void GetHtmlSourceCode() {
		htmlContent = GetHtmlContent.htmlContent(albumUrl);
	}
}
