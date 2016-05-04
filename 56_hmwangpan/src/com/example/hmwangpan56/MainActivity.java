package com.example.hmwangpan56;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.sax.RootElement;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.hmwangpan56.bean.EntryWrapper;
import com.example.hmwangpan56.net.CloudEngine;
import com.example.hmwangpan56.net.ExceptionHandler;
import com.example.hmwangpan56.net.IDataCallBack;
import com.example.hmwangpan56.utils.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vdisk.android.VDiskAuthSession;
import com.vdisk.net.RESTUtility;
import com.vdisk.net.VDiskAPI;
import com.vdisk.net.VDiskAPI.Entry;

public class MainActivity extends SherlockActivity implements IDataCallBack {
	private PullToRefreshListView refreshListView;
	private FileListAdapter adapter;
	VDiskAPI<VDiskAuthSession> mApi;
	private VDiskAuthSession session;
	List<EntryWrapper> contents = new ArrayList<EntryWrapper>();//初始化长度是0
	private ListView lv_list;
	private String curPath = "/";
	private long exitTime;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		initView();
		initListener();
		initData(curPath);
	}

	private void init() {
		actionBar = getSupportActionBar();//得到actionbar,
		actionBar.setDisplayHomeAsUpEnabled(false);//是否显示返回箭头,默认情况是false,
		//如果setDisplayShowHomeEnabled setDisplayShowTitleEnabled 都是false,那么退回图标设置为true/false都不见
		actionBar.setDisplayShowHomeEnabled(false);//是否显示logo,默认是true
		actionBar.setDisplayShowTitleEnabled(true);//是否显示title,默认是true

		//		actionBar.setTitle("56期");//修改title
		//		actionBar.setIcon(R.drawable.icon_download);//修改logo
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getSupportMenuInflater();//这里需要注意使用getSupportMenuInflater不是getMenuInflater
		menuInflater.inflate(R.menu.main, menu);
		uploadMenuItem = menu.findItem(R.id.action_upload);
		downloadMenuItem = menu.findItem(R.id.action_download);
		moreMenuItem = menu.findItem(R.id.action_more);
		selectMenuItem = menu.findItem(R.id.action_select);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			back();
			break;
		case R.id.action_upload:
			Toast.makeText(getApplicationContext(), "action_upload", 0).show();
			break;
		case R.id.action_download:
			Toast.makeText(getApplicationContext(), "action_download", 0).show();
			break;
		case R.id.action_more:
			Toast.makeText(getApplicationContext(), "action_more", 0).show();
			break;
		case R.id.action_createfolder:
			Toast.makeText(getApplicationContext(), "action_createfolder", 0).show();
			break;
		case R.id.action_uploadfile:
			Toast.makeText(getApplicationContext(), "action_uploadfile", 0).show();
			break;
		case R.id.action_logout:
			Toast.makeText(getApplicationContext(), "action_logout", 0).show();
			break;
		case R.id.action_select:
			CharSequence title = selectMenuItem.getTitle();
			if ("全选".equals(title)) {
				selectAll();
				selectMenuItem.setTitle("取消");
			} else if ("取消".equals(title)) {
				unSelectAll();
				selectMenuItem.setTitle("全选");
			}
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void selectAll() {
		for (EntryWrapper entryWrapper : contents) {
			entryWrapper.isCheck = true;
		}
		adapter.notifyDataSetChanged();
		//修改actionbar的title
		seletedCount = contents.size();
		actionBar.setTitle(String.format("已选定%d个", seletedCount));
	}

	private void unSelectAll() {
		for (EntryWrapper entryWrapper : contents) {
			entryWrapper.isCheck = false;
		}
		adapter.notifyDataSetChanged();
		//修改actionbar的title
		seletedCount = 0;
		actionBar.setTitle(String.format("已选定%d个", seletedCount));
	}

	private void initView() {
		rl_root = findViewById(R.id.rl_root);//根布局
		refreshListView = (PullToRefreshListView) findViewById(R.id.lv_list);
		lv_list = refreshListView.getRefreshableView();//需要拿到refreshListView这个组合控件里面的具体的listview,才可以设置adapter
		viewHolder = findViewById(R.id.viewHolder);
	}

	//position true/false;
	private void initListener() {
		refreshListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				//下拉刷新中.我们应该去重写请求数据
				initData(curPath);
				System.out.println("===setOnRefreshListener====");
			}
		});
		lv_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (popupWindow != null) {
					popupWindow.dismiss();
					popupWindow = null;
					return;
				}
				if (position > 0) {
					position -= 1;//1-1=0
				}
				if (isEditModel) {
					adapter.toggleSelect(view, position);
				} else {
					EntryWrapper entry = contents.get(position);
					System.out.println("fileName " + entry.entry.fileName());
					if (entry.entry.isDir) {
						enterFolder(entry);
					}
				}
				//现在任何地方想要发起网络请求也就是一行代码的事情
				//				CloudEngine.getInstance(MainActivity.this).deleteFile(MainActivity.this, entry.path+"aaaa",
				//						CloudEngine.REQ_FILEDELETE);
			}

		});
		lv_list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(getApplicationContext(), "长按", 0).show();
				//开启编辑模式
				startEditModel();
				if (position > 0) {
					position -= 1;
				}
				adapter.toggleSelect(view, position);
				return true;
			}

		});
	}

	private boolean isEditModel;
	private int seletedCount;

	/**
	 * 开启编辑模式
	 */
	private void startEditModel() {
		//listview需要刷新
		isEditModel = true;
		adapter.notifyDataSetChanged();
		//修改actionbar
		uploadMenuItem.setVisible(false);
		downloadMenuItem.setVisible(false);
		moreMenuItem.setVisible(false);
		selectMenuItem.setVisible(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(String.format("已选定%d个", seletedCount));
		//显示底部的popupwindows
		showBottomPopupWindow();
		//listview上移
		viewHolder.setVisibility(0);
	}

	/**
	 * 结束编辑模式
	 */
	private void stopEditModel() {
		//listview需要刷新
		isEditModel = false;
		adapter.notifyDataSetChanged();
		//修改actionbar
		uploadMenuItem.setVisible(true);
		downloadMenuItem.setVisible(true);
		moreMenuItem.setVisible(true);
		selectMenuItem.setVisible(false);
		actionBar.setTitle("黑马网盘");
		//返回按钮的处理
		if ("/".equals(curPath)) {
			actionBar.setDisplayHomeAsUpEnabled(false);
		}
		//隐藏popupwindows
		bottomPopupWindow.dismiss();
		//listview还原
		viewHolder.setVisibility(8);
		//还原entryWrapper的选中状态
		for (EntryWrapper entryWrapper : contents) {
			entryWrapper.isCheck = false;
		}
		seletedCount = 0;
	}

	private void showBottomPopupWindow() {
		if (bottomPopupWindow == null) {
			View contentView = View.inflate(MainActivity.this, R.layout.bottom_edit_pop, null);
			int width = ViewGroup.LayoutParams.FILL_PARENT;
			int height = ViewGroup.LayoutParams.WRAP_CONTENT;
			bottomPopupWindow = new PopupWindow(contentView, width, height);
			contentView.findViewById(R.id.DeleteBtn).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					List<EntryWrapper> selectedEntryWrappers = new ArrayList<EntryWrapper>();
					for (EntryWrapper info : contents) {
						if (info.isCheck) {
							selectedEntryWrappers.add(info);
						}
					}
					StringBuffer sb = new StringBuffer();
					//遍历输出
					for (EntryWrapper entryWrapper : selectedEntryWrappers) {
						sb.append(entryWrapper.entry.fileName()).append(" ");
					}
					System.out.println(sb.toString());
				}
			});
		}
		bottomPopupWindow.showAtLocation(rl_root, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
	}

	/**
	 * 进入文件夹
	 * @param entry
	 */
	private void enterFolder(EntryWrapper entry) {
		actionBar.setDisplayHomeAsUpEnabled(true);//进入子目录.显示返回箭头
		initData(entry.entry.path);
		curPath = entry.entry.path;//记录当前的path
		System.out.println("curPath:" + curPath);
	}

	private void initData(String path) {
		CloudEngine instance = CloudEngine.getInstance(this);
		instance.getFileList(this, CloudEngine.REQ_FILELIST, path);
		refreshListView.setRefreshing();//开始加载数据的时候.应该显示.正在刷新
		adapter = new FileListAdapter();
		lv_list.setAdapter(adapter);

	}

	private PopupWindow popupWindow;
	private int curClickPostion;
	private PopupWindow bottomPopupWindow;
	private View rl_root;
	private MenuItem uploadMenuItem;
	private MenuItem downloadMenuItem;
	private MenuItem moreMenuItem;
	private MenuItem selectMenuItem;
	private View viewHolder;

	class FileListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO
			return contents.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO
			return contents.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			System.out.println("==========getView==========");
			ViewHolder holder;
			if (convertView == null) {
				convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.file_item, null);

				holder = new ViewHolder();
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
				holder.tvSize = (TextView) convertView.findViewById(R.id.tv_size);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.ivOption = (ImageView) convertView.findViewById(R.id.iv_option);
				holder.cbCheck = (CheckBox) convertView.findViewById(R.id.cb_checkbox);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			//数据填充
			EntryWrapper entryWrapper = contents.get(position);
			holder.tvName.setText(entryWrapper.entry.fileName());
			String modified = entryWrapper.entry.modified;
			Date parseDate = RESTUtility.parseDate(modified);
			String formateTime = Utils.getFormateTime(parseDate);
			holder.tvTime.setText(formateTime);
			if (entryWrapper.entry.isDir) {
				holder.tvSize.setVisibility(8);
				holder.ivIcon.setImageResource(R.drawable.directory_icon);
			} else {
				holder.tvSize.setVisibility(0);
				//根据不同文件的mime类型呢显示不同图片
				Object[] mimeType = Utils.getMIMEType(entryWrapper.entry.fileName());
				holder.ivIcon.setImageResource((Integer) mimeType[1]);
			}
			/*点击显示popupwindow*/
			holder.ivOption.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					curClickPostion = position;
					showPopupWindow(v);
				}

			});
			/**根据是否是编辑模式.修改界面*/
			if (isEditModel) {
				holder.ivOption.setVisibility(8);
				holder.cbCheck.setVisibility(0);
				boolean isCheck = entryWrapper.isCheck;
				if (isCheck) {
					holder.cbCheck.setChecked(true);
				} else {
					holder.cbCheck.setChecked(false);
				}
			} else {
				holder.ivOption.setVisibility(0);
				holder.cbCheck.setVisibility(8);
			}
			return convertView;
		}

		/**
		 * @param itemView 当前点击的itemView
		 * @param position 当前的position
		 */
		public void toggleSelect(View itemView, int position) {
			EntryWrapper entryWrapper = contents.get(position);
			CheckBox checkBox = (CheckBox) itemView.findViewById(R.id.cb_checkbox);
			if (checkBox.isChecked()) {
				checkBox.setChecked(false);
				entryWrapper.isCheck = false;//数据保存
				//修改actionbar的title
				seletedCount--;
				actionBar.setTitle(String.format("已选定%d个", seletedCount));
				if (seletedCount < contents.size()) {
					selectMenuItem.setTitle("全选");
				}
			} else {
				checkBox.setChecked(true);
				entryWrapper.isCheck = true;//数据保存
				//修改actionbar的title
				seletedCount++;
				actionBar.setTitle(String.format("已选定%d个", seletedCount));
				if (seletedCount == contents.size()) {
					selectMenuItem.setTitle("取消");
				}
			}
		}

		/*
		 * 显示popupWindow
		 */
		private void showPopupWindow(View v) {
			View itemView = (View) v.getParent();
			if (popupWindow == null) {
				View contentView = View.inflate(MainActivity.this, R.layout.file_item_pop, null);
				int width = ViewGroup.LayoutParams.MATCH_PARENT;
				int height = itemView.getHeight();
				System.out.println("height:" + height);
				popupWindow = new PopupWindow(contentView, width, height);
				/*点击popupWindow范围以外的地方,让popupWindow消失*/
				popupWindow.setOutsideTouchable(true);
				popupWindow.setBackgroundDrawable(new BitmapDrawable());
				//点击事件
				contentView.findViewById(R.id.ll_delete).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						popupWindow.dismiss();
						popupWindow = null;
						EntryWrapper entry = contents.get(curClickPostion);
						CloudEngine.getInstance(MainActivity.this).deleteFile(MainActivity.this, entry.entry.path,
								CloudEngine.REQ_FILEDELETE);
					}
				});
			}
			//显示控制
			if (isShowBottom(itemView)) {
				popupWindow.showAsDropDown(itemView, 0, 0);
			} else {
				popupWindow.showAsDropDown(itemView, 0, -2 * itemView.getHeight());
			}

		}

		private boolean isShowBottom(View itemView) {
			int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
			int[] location = new int[2];
			//location[0]-->x
			//location[1]-->y
			itemView.getLocationInWindow(location);
			int itemViewY = location[1];
			int distance = screenHeight - itemViewY - itemView.getHeight();
			if (distance < itemView.getHeight() + 3) {
				return false;
			}
			return true;
		}

		class ViewHolder {
			TextView tvName;
			TextView tvTime;
			TextView tvSize;

			ImageView ivIcon;
			ImageView ivOption;

			CheckBox cbCheck;
		}
	}

	//文件夹信息列表
	//文件删除
	//因为以后所有的请求的结果都是跑到handleServerData里面来.所以为了区分处理.我们需要加上reqCode
	/**所有请求的返回结果,就在这个回调方法里面处理*/
	@Override
	public void handleServerData(int reqCode, int errCode, Object obj) {
		if (errCode != 0) {//0:正常 
			//errCode和String.xml中年定义的异常内容要对应起来
			ExceptionHandler.toastErrMessage(MainActivity.this, errCode);
			return;
		}
		switch (reqCode) {
		case CloudEngine.REQ_FILELIST:
			refreshListView.onRefreshComplete();//结束刷新状态
			List<Entry> entrys = (List<Entry>) obj;//获取列表成功,重新赋值,修改数据源
			for (Entry entry : entrys) {
				EntryWrapper entryWrapper = new EntryWrapper();
				entryWrapper.entry = entry;
				contents.add(entryWrapper);
			}
			System.out.println("contents.size():" + contents.size());
			adapter.notifyDataSetChanged();//刷新了listview
			break;
		case CloudEngine.REQ_FILEDELETE:
			Entry entry = (Entry) obj;//请求返回的entry
			EntryWrapper entryWrapper = new EntryWrapper();//封装到我们的entryWrapper中
			entryWrapper.entry = entry;
			//方式一
			contents.remove(entryWrapper);
			adapter.notifyDataSetChanged();
			//方式二
			/*String path = entry.path;
			for (EntryWrapper info : contents) {
				if (info.entry.path.equals(path)) {
					System.out.println("进入了if");
					contents.remove(info);
				}
			}
			adapter.notifyDataSetChanged();*/
			//方式三
			/*String path = entry.path;
			Iterator<Entry> iterator = contents.iterator();
			while (iterator.hasNext()) {
				Entry next = iterator.next();
				if (path.equals(next.path)) {
					iterator.remove();
				}
			}
			adapter.notifyDataSetChanged();*/
			Toast.makeText(getApplicationContext(), "删除 " + entry.fileName() + " 成功", 0).show();
			break;

		default:
			break;
		}
	}

	/*
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {//按下了返回键
				System.out.println("按下了返回键");
			}
			return true;
	//		return super.onKeyDown(keyCode, event);
		}*/
	/**
	 *  /
	 *	/种子   ---->执行substring之后变成""---->实际我想要的是"/"
	 *	/种子/苍老师---->/种子(这个没有任何问题)
	 */
	@Override
	public void onBackPressed() {
		back();
	}

	/**
	 * 处理回退逻辑
	 */
	public void back() {
		if (isEditModel) {
			stopEditModel();
			return;
		}
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
			popupWindow = null;
			return;
		}
		if ("/".equals(curPath)) {//根目录
			if (System.currentTimeMillis() - exitTime > 2000) {//
				Toast.makeText(getApplicationContext(), "再按一次,退出黑马网盘", 0).show();
				exitTime = System.currentTimeMillis();
				return;
			} else {
				finish();
				return;
			}
		}
		curPath = curPath.substring(0, curPath.lastIndexOf("/"));
		if ("".equals(curPath)) {
			curPath = "/";
			actionBar.setDisplayHomeAsUpEnabled(false);//如果是根目录.隐藏返回箭头
		}
		initData(curPath);
	}
}
