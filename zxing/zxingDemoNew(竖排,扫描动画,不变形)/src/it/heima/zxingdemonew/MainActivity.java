package it.heima.zxingdemonew;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final int SCANNIN_GREQUEST_CODE = 1;
	private Button bt_scan;
	private TextView tv_result;
	private ImageView iv_qrcode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initAction();
	}

	private void initView() {
		bt_scan = (Button) findViewById(R.id.bt_scan);
		tv_result = (TextView) findViewById(R.id.tv_result);
		iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
	}

	private void initAction() {
		bt_scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//1.启动扫描界面
				Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
			}
		});

	}
	//3.收到回传的 结果
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SCANNIN_GREQUEST_CODE:
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				//显示扫描到的内容
				tv_result.setText(bundle.getString("result"));
				//显示
				iv_qrcode.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
			}
			break;
		}
	}
}
