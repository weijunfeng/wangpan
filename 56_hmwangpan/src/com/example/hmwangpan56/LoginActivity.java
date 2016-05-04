package com.example.hmwangpan56;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.vdisk.android.VDiskAuthSession;
import com.vdisk.android.VDiskDialogListener;
import com.vdisk.net.exception.VDiskDialogError;
import com.vdisk.net.exception.VDiskException;
import com.vdisk.net.session.AccessToken;
import com.vdisk.net.session.AppKeyPair;
import com.vdisk.net.session.Session.AccessType;

public class LoginActivity extends Activity implements VDiskDialogListener {
	/*这个是自己申请的账号.没有basic权限*/
	/**
	 * 开发者应用默认可以访问 sandbox 容器，如需要申请 basic 访问权限，请发邮件到 vdiskapi@sina.cn，内容如下：
	appkey ：你的appkey信息
	应用名称：你的应用名称信息
	用途,应用场景(详细表述) : 你的应用场景(用途)信息
	应用主页：你的应用主页信息
	应用下载地址：你的应用url下载地址
	应用截图（4张）: 你的应用截图信息，4张图片
	关于用户空间隔离介绍请点击用户空间隔离
	 */
	/*下面这个appid是老师自己申请的.没有一个basic权限.不能访问操作微盘官方网盘内的文件,
	 * 为了继续开发演示.我们使用VDiskSdk_Example里面的appkey,appsecret,redirecturl
	 * */
	protected static final String REDIRECT_URL = "http://vauth.appsina.com/callback1.php";
	public static final String CONSUMER_KEY = "2330724462";
	public static final String CONSUMER_SECRET = "04f81fc56cc936bfc8f0fa1cef285158";
	private VDiskAuthSession session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		init();
		findViewById(R.id.btn1).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//1.发起授权请求
				// 使用微盘Token认证，需设置重定向网址
				// Need to set REDIRECT_URL if you want to use VDisk token.
				//回调地址的作用:重定向的时候接受我们的授权码-->
				session.setRedirectUrl(REDIRECT_URL);
				session.authorize(LoginActivity.this, LoginActivity.this);
			}
		});
		if (session.isLinked()) {//如果有accessToken和secret
			//跳到主界面
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
	}

	private void init() {
		/**
		 * 初始化 Init
		 */
		AppKeyPair appKeyPair = new AppKeyPair(CONSUMER_KEY, CONSUMER_SECRET);
		/**
		 * @AccessType.APP_FOLDER - sandbox 模式
		 * @AccessType.VDISK - basic 模式-->需要去申请
		 */
		/**
		 * 测试环境  
		 * 正式环境 
		 */
		session = VDiskAuthSession.getInstance(this, appKeyPair, AccessType.VDISK);//session是一个单例
		//		session = new VDiskAuthSession(appKeyPair, AccessType.VDISK);
	}

	/**2.处理授权结果.拿到accessToken*/
	@Override
	public void onComplete(Bundle values) {//授权成功
		// TODO
		Toast.makeText(getApplicationContext(), "onComplete", 0).show();
		if (values != null) {
			AccessToken mToken = (AccessToken) values.getSerializable(VDiskAuthSession.OAUTH2_TOKEN);
			session.finishAuthorize(mToken);//token赋值给session对象
			System.out.println("token: " + mToken.getToken());
		}
		//授权成功-->跳到主界面
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	@Override
	public void onError(VDiskDialogError error) {//授权失败
		// TODO
		Toast.makeText(getApplicationContext(), "onError", 0).show();

	}

	@Override
	public void onVDiskException(VDiskException exception) {//授权出现异常
		// TODO
		Toast.makeText(getApplicationContext(), "onVDiskException", 0).show();

	}

	@Override
	public void onCancel() {//授权取消
		// TODO
		Toast.makeText(getApplicationContext(), "onCancel", 0).show();

	}

}
