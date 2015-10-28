package com.huewu.pla.sample;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.dodola.model.DuitangInfo;
import com.dodowaterfall.widget.ScaleImageView;
import com.example.android.bitmapfun.util.ImageFetcher;
import com.huewu.pla.lib.internal.PLA_AdapterView;
import com.huewu.pla.lib.internal.PLA_AdapterView.OnItemClickListener;

@SuppressLint("InflateParams")
public class PullToRefreshSampleActivity extends FragmentActivity implements IXListViewListener {
    private ImageFetcher mImageFetcher;
    private XListView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private int currentPage = 0;
    private List<String> albumurl = new ArrayList<String>();
    ContentTask task = new ContentTask(this, 2);

    private class ContentTask extends AsyncTask<String, Integer, List<DuitangInfo>> {

        private Context mContext;
        private int mType = 1;

        public ContentTask(Context context, int type) {
            super();
            mContext = context;
            mType = type;
        }

        @Override
        protected List<DuitangInfo> doInBackground(String... params) {
			List<DuitangInfo> list = new ArrayList<DuitangInfo>();
        	String htmlContent = GetHtmlContent.htmlContent("http://www.5442.com/meinv/");
        	if (htmlContent.contains("container")) {
				htmlContent = htmlContent.substring(
						htmlContent.indexOf("container"),
						htmlContent.indexOf("</ul>",
								htmlContent.indexOf("container")));
				while (htmlContent.contains("m.5442.com")
						&& htmlContent.contains("libox")) {
					int linkStart = htmlContent.indexOf(
							"http://m.5442.com",
							htmlContent.indexOf("libox"));
					int linkEnd = htmlContent
							.indexOf("html", linkStart) + 4;
					String linkUrl = htmlContent.substring(linkStart,
							linkEnd);
					int imageStart = htmlContent.indexOf(
							"http://pic2015.5442.com",
							htmlContent.indexOf("libox"));
					int imageEnd = htmlContent.indexOf("jpg",
							imageStart) + 3;
					String imageUrl = htmlContent.substring(imageStart,
							imageEnd) + "!400.jpg";
					int imageNameStart = htmlContent.indexOf("alt")+5;
					int imageNameEnd = htmlContent.indexOf("\"", imageNameStart);
					String imageName = htmlContent.substring(imageNameStart, imageNameEnd);
					imageName = imageName.replaceAll("<b>", "");
					imageName = imageName.replaceAll("</b>", "");
					try {
						htmlContent = htmlContent
								.substring(htmlContent
										.indexOf("libox", htmlContent
												.indexOf("libox") + 5));
					} catch (Exception e) {
						// 已经读到文件尾了
						break;
					}
					DuitangInfo dti = new DuitangInfo();
					dti.setAlbid(System.currentTimeMillis()+"");
					dti.setIsrc(imageUrl);
					dti.setMsg(imageName);
					dti.setHeight(200);
					list.add(dti);
					albumurl.add(linkUrl);
				}
			}
            return list;
        }

        @Override
        protected void onPostExecute(List<DuitangInfo> result) {
            if (mType == 1) {

                mAdapter.addItemTop(result);
                mAdapter.notifyDataSetChanged();
                mAdapterView.stopRefresh();

            } else if (mType == 2) {
                mAdapterView.stopLoadMore();
                mAdapter.addItemLast(result);
                mAdapter.notifyDataSetChanged();
            }

        }

        @Override
        protected void onPreExecute() {
        }

    }

    /**
     * 添加内容
     * 
     * @param pageindex
     * @param type
     *            1为下拉刷新 2为加载更多
     */
    private void AddItemToContainer(int pageindex, int type) {
        if (task.getStatus() != Status.RUNNING) {
            String url = "http://www.duitang.com/album/1733789/masn/p/" + pageindex + "/24/";
            Log.d("MainActivity", "current url:" + url);
            ContentTask task = new ContentTask(this, type);
            task.execute(url);

        }
    }

    public class StaggeredAdapter extends BaseAdapter {
        private Context mContext;
        private LinkedList<DuitangInfo> mInfos;
        private XListView mListView;

        public StaggeredAdapter(Context context, XListView xListView) {
            mContext = context;
            mInfos = new LinkedList<DuitangInfo>();
            mListView = xListView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            DuitangInfo duitangInfo = mInfos.get(position);

            if (convertView == null) {
                LayoutInflater layoutInflator = LayoutInflater.from(parent.getContext());
                convertView = layoutInflator.inflate(R.layout.infos_list, null);
                holder = new ViewHolder();
                holder.imageView = (ScaleImageView) convertView.findViewById(R.id.news_pic);
                holder.contentView = (TextView) convertView.findViewById(R.id.news_title);
                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();
            holder.imageView.setImageWidth(duitangInfo.getWidth());
            holder.imageView.setImageHeight(duitangInfo.getHeight());
            holder.contentView.setText(duitangInfo.getMsg());
            mImageFetcher.loadImage(duitangInfo.getIsrc(), holder.imageView);
            return convertView;
        }

        class ViewHolder {
            ScaleImageView imageView;
            TextView contentView;
            TextView timeView;
        }

        @Override
        public int getCount() {
            return mInfos.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mInfos.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        public void addItemLast(List<DuitangInfo> datas) {
            mInfos.addAll(datas);
        }

        public void addItemTop(List<DuitangInfo> datas) {
            for (DuitangInfo info : datas) {
                mInfos.addFirst(info);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pull_to_refresh_sample);
        mAdapterView = (XListView) findViewById(R.id.list);
        mAdapterView.setPullLoadEnable(false);
        mAdapterView.setPullRefreshEnable(false);
        mAdapterView.setXListViewListener(this);

        mAdapter = new StaggeredAdapter(this, mAdapterView);

        mImageFetcher = new ImageFetcher(this, 240);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        
        mAdapterView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(PLA_AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.setClass(PullToRefreshSampleActivity.this, AlbumActivity.class);
				intent.putExtra("albumUrl", albumurl.get(position-1));
				startActivity(intent);
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapterView.setAdapter(mAdapter);
        AddItemToContainer(currentPage, 2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRefresh() {
        AddItemToContainer(++currentPage, 1);

    }

    @Override
    public void onLoadMore() {
        AddItemToContainer(++currentPage, 2);

    }
    
  //双击返回键退出
    private long exitTime = 0;
	public void onBackPressed() {
		if ((System.currentTimeMillis() - exitTime) > 3000)
        {
                Toast.makeText(PullToRefreshSampleActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
        } else
        {
        	PullToRefreshSampleActivity.this.finish();
        }
	}
}// end of class
