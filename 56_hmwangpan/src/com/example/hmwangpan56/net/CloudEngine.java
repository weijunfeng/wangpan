package com.example.hmwangpan56.net;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.example.hmwangpan56.LoginActivity;
import com.vdisk.android.VDiskAuthSession;
import com.vdisk.net.VDiskAPI;
import com.vdisk.net.VDiskAPI.Entry;
import com.vdisk.net.exception.VDiskException;
import com.vdisk.net.exception.VDiskFileNotFoundException;
import com.vdisk.net.exception.VDiskFileSizeException;
import com.vdisk.net.exception.VDiskIOException;
import com.vdisk.net.exception.VDiskLocalStorageFullException;
import com.vdisk.net.exception.VDiskParseException;
import com.vdisk.net.exception.VDiskPartialFileException;
import com.vdisk.net.exception.VDiskServerException;
import com.vdisk.net.exception.VDiskUnlinkedException;
import com.vdisk.net.session.AppKeyPair;
import com.vdisk.net.session.Session.AccessType;

/**
 * 网络请求引擎,统一所有网络的访问
 * @author Administrator
 *
 */
public class CloudEngine {
	private static CloudEngine instance;
	private VDiskAuthSession session;
	private VDiskAPI mApi;
	public static final int REQ_FILELIST = 100;
	public static final int REQ_FILEDELETE = 101;
	private Context context;

	private CloudEngine(Context context) {
		init(context);
		this.context = context;
	}

	private void init(Context context) {
		AppKeyPair appKeyPair = new AppKeyPair(LoginActivity.CONSUMER_KEY, LoginActivity.CONSUMER_SECRET);
		session = VDiskAuthSession.getInstance(context, appKeyPair, AccessType.VDISK);
		mApi = new VDiskAPI<VDiskAuthSession>(session);

	}

	public static CloudEngine getInstance(Context context) {
		if (instance == null) {
			synchronized (CloudEngine.class) {
				if (instance == null) {
					instance = new CloudEngine(context);
				}
			}
		}
		return instance;
	}

	/**
	 * 获取文件列表
	 * @param callaback
	 * @param path 
	 */
	public void getFileList(IDataCallBack callaback, int reqCode, String path) {
		new FileListTask(callaback, reqCode, path).execute();
	}

	//handler 
	//接口回调
	class FileListTask extends BaseTask {
		private String path;

		public FileListTask(IDataCallBack callaback, int reqCode, String path) {//通过构造方法实例化
			super(callaback, reqCode);
			this.path = path;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				//int reqCode-->区别请求 -->请求的统一处理
				//int errCode -->区别错误-->错误的统一处理
				//Object data-->请求得到的数据库
				Entry metadata = mApi.metadata(path, null, true, false);
				List<Entry> contents = metadata.contents;//文件列表
				event.data = contents;
			} catch (VDiskException e) {
				e.printStackTrace();
				updateEvent(context, e, event);
			}
			return null;
		}
	}

	/**
	 * 文件的删除
	 * @param path
	 */
	public void deleteFile(IDataCallBack callback, String path, int reqCode) {
		new DeleteFileTask(callback, path, reqCode).execute();
	}

	class DeleteFileTask extends BaseTask {
		String path;

		public DeleteFileTask(IDataCallBack callback, String path, int reqCode) {
			super(callback, reqCode);
			this.path = path;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Entry metaData = mApi.delete(path);
				event.data = metaData;
			} catch (VDiskException e) {
				e.printStackTrace();
				updateEvent(context, e, event);
			}
			return null;
		}
	}

	/**
	 * 抽取一些共性的东西到基类
	 * @author Administrator
	 *
	 */
	class BaseTask extends AsyncTask<Void, Void, Void> {
		IDataCallBack callback;//定义接口对象
		Event event;
		int reqCode;

		public BaseTask(IDataCallBack callback, int reqCode) {
			super();
			this.callback = callback;
			this.event = new Event();
			event.reqCode = reqCode;
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			callback.handleServerData(event.reqCode, event.errCode, event.data);
			super.onPostExecute(result);
		}
	}

	/**
	 * 根本不同的异常,对event.errCode赋予不同的值
	 * @param ctx
	 * @param e 异常的基类
	 * @param event  event
	 * @return
	 */
	public static Event updateEvent(Context ctx, VDiskException e, Event event) {
		if (event == null) {
			event = new Event();
		}
		if (e instanceof VDiskServerException) {
			return ExceptionHandler.getErrEvent(ctx, (VDiskServerException) e, event);
		} else if (e instanceof VDiskIOException) {
			event.errCode = ExceptionHandler.VdiskConnectionFailureErrorType;
		} else if (e instanceof VDiskParseException) {
			event.errCode = ExceptionHandler.kVdiskErrorInvalidResponse;
		} else if (e instanceof VDiskLocalStorageFullException) {
			event.errCode = ExceptionHandler.kVdiskErrorInsufficientDiskSpace;
		} else if (e instanceof VDiskUnlinkedException) {
			event.errCode = ExceptionHandler.UNLINKED_ERROR;
		} else if (e instanceof VDiskFileSizeException) {
			event.errCode = ExceptionHandler.FILE_TOO_BIG_ERROR;
		} else if (e instanceof VDiskPartialFileException) {
			event.errCode = ExceptionHandler.PARTIAL_FILE_ERROR;
		} else if (e instanceof VDiskFileNotFoundException) {
			event.errCode = ExceptionHandler.FILE_NOT_FOUND;
		} else {
			event.errCode = ExceptionHandler.OTHER_ERROR;
		}
		return event;
	}
}
