package com.example.appupdatedemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

	// ���°汾Ҫ�õ���һЩ��Ϣ
	private UpdateInfo info;
	private ProgressDialog pBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toast.makeText(MainActivity.this, "���ڼ��汾����..", Toast.LENGTH_SHORT).show();
		// �Զ������û���°汾 ������°汾����ʾ����
		new Thread() {
			public void run() {
				try {
					UpdateInfoService updateInfoService = new UpdateInfoService(
							MainActivity.this);
					info = updateInfoService.getUpDateInfo();
					handler1.sendEmptyMessage(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	@SuppressLint("HandlerLeak")
	private Handler handler1 = new Handler() {
		public void handleMessage(Message msg) {
			// ����и��¾���ʾ
			if (isNeedUpdate()) {
				showUpdateDialog();
			}
		};
	};

	private void showUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("������APP���汾" + info.getVersion());
		builder.setMessage(info.getDescription());
		builder.setCancelable(false);

		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					downFile(info.getUrl());
				} else {
					Toast.makeText(MainActivity.this, "SD�������ã������SD��",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}

		});
		builder.create().show();
	}

	private boolean isNeedUpdate() {
		
		String v = info.getVersion(); // ���°汾�İ汾��
		Log.i("update",v);
		Toast.makeText(MainActivity.this, v, Toast.LENGTH_SHORT).show();
		if (v.equals(getVersion())) {
			return false;
		} else {
			return true;
		}
	}

	// ��ȡ��ǰ�汾�İ汾��
	private String getVersion() {
		try {
			PackageManager packageManager = getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "�汾��δ֪";
		}
	}

	void downFile(final String url) { 
		pBar = new ProgressDialog(MainActivity.this);    //�������������ص�ʱ��ʵʱ���½��ȣ�����û��Ѻö�
		pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pBar.setTitle("��������");
		pBar.setMessage("���Ժ�...");
		pBar.setProgress(0);
		pBar.show();
		new Thread() {
			public void run() {        
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					int length = (int) entity.getContentLength();   //��ȡ�ļ���С
                                        pBar.setMax(length);                            //���ý��������ܳ���
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if (is != null) {
						File file = new File(
								Environment.getExternalStorageDirectory(),
								"Test.apk");
						fileOutputStream = new FileOutputStream(file);
						//����ǻ���������һ�ζ�ȡ10�����أ���Ū��С�˵㣬��Ϊ�ڱ��أ�������ֵ̫��һ�¾���������,
						//������progressbar��Ч����
                        byte[] buf = new byte[10];   
						int ch = -1;
						int process = 0;
						while ((ch = is.read(buf)) != -1) {       
							fileOutputStream.write(buf, 0, ch);
							process += ch;
							pBar.setProgress(process);       //������ǹؼ���ʵʱ���½����ˣ�
						}

					}
					fileOutputStream.flush();
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
					down();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}.start();
	}

	void down() {
		handler1.post(new Runnable() {
			public void run() {
				pBar.cancel();
				update();
			}
		});
	}

	void update() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), "Test.apk")),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

}