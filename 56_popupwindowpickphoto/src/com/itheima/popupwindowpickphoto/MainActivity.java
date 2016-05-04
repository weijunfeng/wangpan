package com.itheima.popupwindowpickphoto;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	protected static final int CODE_TAKE_PHOTO = 100;
	protected static final int CODE_PICK_PHOTO = 101;
	private static final int CODE_ZOOM_PHOTOT = 102;
	private String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	private File tempFile = new File(sdCardPath + "/" + "tempFile.jpg");

	private View root;
	private SelectPhotoPopupWindow selectPhotoPopupWindow;

	private View viewMask;
	private CheckBox cb;
	private CircleImageView civ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		viewMask = findViewById(R.id.viewMask);
		root = findViewById(R.id.rl_root);
		cb = (CheckBox) findViewById(R.id.cb);
		civ = (CircleImageView) findViewById(R.id.civ);
		civ.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (cb.isChecked()) {
					PhotoUtilChange.getPhotoDialog(MainActivity.this, CODE_TAKE_PHOTO, CODE_PICK_PHOTO, tempFile);
				} else {
					selectPhotoPopupWindow = PhotoUtilChange.getPicPopupWindow(MainActivity.this, MainActivity.this,
							root);
					AnimationUtils.showAlpha(viewMask);
				}
			}
		});
		viewMask.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectPhotoPopupWindow != null) {
					selectPhotoPopupWindow.dismiss();
					AnimationUtils.hideAlpha(viewMask);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		selectPhotoPopupWindow.dismiss();
		switch (v.getId()) {
		case R.id.btn_cancel:
			Toast.makeText(getApplicationContext(), "cancle", 0).show();
			AnimationUtils.hideAlpha(viewMask);
			break;
		case R.id.btn_take_photo://拍照
			//1.发起拍照的intent
			PhotoUtilChange.takePhoto(MainActivity.this, CODE_TAKE_PHOTO, tempFile);
			break;
		case R.id.btn_pick_photo://从相册选择
			//1.发起从相册选择的intent
			PhotoUtilChange.pickPhoto(MainActivity.this, CODE_PICK_PHOTO, tempFile);
			break;
		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CODE_TAKE_PHOTO://拍照
			//2.处理拍照的结果-->去裁剪
			PhotoUtilChange.onPhotoFromCamera(MainActivity.this, CODE_ZOOM_PHOTOT, tempFile.getAbsolutePath(), 1, 1);
			break;
		case CODE_PICK_PHOTO://从相册选择
			//2.处理从相册选择的结果-->去裁剪
			PhotoUtilChange.onPhotoFromPick(MainActivity.this, CODE_ZOOM_PHOTOT, tempFile.getAbsolutePath(), data, 50,
					50);
			break;
		case CODE_ZOOM_PHOTOT://裁剪
			//3.裁剪完成
			Bitmap zoomBitMap = PhotoUtilChange.getZoomBitMap(data, MainActivity.this);
			//4.修改头像
			civ.setImageBitmap(zoomBitMap);
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
