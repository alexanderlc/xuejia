package com.xuejia;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
	private MainActivity mThis;
	private RelativeLayout mStartupLayout;
	private LinearLayout mImageLayout;
	private WebView mWebView;
	private String mURL;
	private ProgressBar mProgressBar;
	private static final int STOPSPLASH = 0;
	private static final int CONNECTION_FALIED = -1;
	private static final int OVERDUE=-2;
	// time in milliseconds
	//private static final long SPLASHTIME = 1000;

	@SuppressLint("HandlerLeak")
	private Handler splashHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STOPSPLASH:
				SystemClock.sleep(2000);	
				mStartupLayout.setVisibility(View.GONE);
				break;
			case CONNECTION_FALIED:
				new AlertDialog.Builder(mThis).setTitle(mThis.getString(R.string.app_name))
				.setMessage("无法连接服务器，请检查您的网络设置并重新启动程序。").setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						finish();
					}
				}).show();
				break;
			case  OVERDUE:
				new AlertDialog.Builder(mThis).setTitle(mThis.getString(R.string.app_name))
				.setMessage("演示程序已经过期,请到官网下载正式版").setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						finish();
					}
				}).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main_back);
		mThis=this;
		findViews();
		loadSplash();
		initWebView();
		getStartImage();
		//splash();
	}
	private void findViews(){
		mImageLayout=(LinearLayout) this.findViewById(R.id.layout_img);
		mStartupLayout=(RelativeLayout) this.findViewById(R.id.layout_splashscreen);
		mWebView=(WebView) this.findViewById(R.id.webview);
		mProgressBar=(ProgressBar) this.findViewById(R.id.progressBar);
	}
	private void loadSplash(){
		String colorStr=PreferencesManager.getBgColor(mThis);
		String img=PreferencesManager.getBgImage(mThis);
		File imgFile=new File(img);
		if(imgFile.exists()){
			Drawable d=Drawable.createFromPath(img);
			mImageLayout.setBackgroundDrawable(d);
			int color=Color.parseColor(colorStr);
			mStartupLayout.setBackgroundColor(color);
		}
	}
	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView(){
		mWebView.setHorizontalScrollBarEnabled(false);// 取消Horizontal ScrollBar显示
		mWebView.setVerticalFadingEdgeEnabled(true);
		WebSettings bs = mWebView.getSettings();
		bs.setBuiltInZoomControls(false);
		bs.setSupportZoom(true);
		//加速WebView加载的方法,	提高渲染的优先级
		bs.setRenderPriority(RenderPriority.HIGH); 	
		bs.setUseWideViewPort(false);
		bs.setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			public void onPageFinished(WebView view, String url) {
				mProgressBar.setVisibility(View.GONE);
				super.onPageFinished(view, url);
			}

			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mProgressBar.setVisibility(View.VISIBLE);
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl){
				mProgressBar.setVisibility(View.GONE);
			}
		});		
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override 
			public void onReceivedTitle(WebView view, String title) { 
			} 
			public void onProgressChanged(WebView view, int progress) {
				mProgressBar.setProgress(progress);
			}
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				result.confirm();
				return true;
			}
		});
		mURL=mThis.getString(R.string.url_home);
		mWebView.loadUrl(mURL);
	}

	
	@Override
	public void onBackPressed(){
		if(mWebView.canGoBack()){
			mWebView.goBack();
		}else{
			exit();
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK ) {
			if(mWebView.canGoBack()){
				mWebView.goBack();
			}else{
				exit();
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	private void exit(){
		new AlertDialog.Builder(this).setTitle("提示")
		.setMessage("确定退出雪茄超市？").setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				finish();
			}
		}).setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				// 取消按钮事件
			}
		}).show();
	}
	private void getStartImage(){
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					Calendar calendar = Calendar.getInstance();
					Date now = calendar.getTime();					
					SimpleDateFormat DateFormat = new SimpleDateFormat(
							"yyyy-MM-dd"); 
					Date overdueDt=DateFormat.parse("2013-07-25");
					if(now.after(overdueDt)){
						msg.what = OVERDUE;
						splashHandler.sendMessage(msg);
						return;
					}
					if(NetworkUtils.getActiveNetworkName(mThis)==null){
						//无网络
						msg.what = CONNECTION_FALIED;
						splashHandler.sendMessage(msg);
					}else{
						//下载xml
						String startConfigURL=getString(R.string.url_start_config);
						String xml=NetworkUtils.getHtmlResponse(mThis, startConfigURL);
						if(xml!=null&&!xml.equals("")){
							msg.what = STOPSPLASH;
							splashHandler.sendMessage(msg);
							StartConfig sc=ConfigXMLParser.parse(xml);
							if(sc!=null){
								String url=sc.ImageURL;
								String fileName=NetworkUtils.getFileName(url);
								File root = Environment.getExternalStorageDirectory();
								File gbDir = new File(root, "/xuejia/");
								if (!gbDir.exists()) {
									gbDir.mkdir();
								}
								String savepath=gbDir.getAbsolutePath()+"/"+fileName;
								File imgFile=new File(savepath);
								if(!imgFile.exists()){
									//下载
									if(NetworkUtils.downloadAndSave(mThis, url, savepath)){
										PreferencesManager.setBgImage(mThis,savepath);
										PreferencesManager.setBgColor(mThis, sc.BackgroundColor);
									}else{
										savepath="";
									}
								}else{
									PreferencesManager.setBgImage(mThis,savepath);
									PreferencesManager.setBgColor(mThis, sc.BackgroundColor);
								}
							}//id sc
						}else{
							msg.what = CONNECTION_FALIED;
							splashHandler.sendMessage(msg);
						}
					}
				}catch(Exception e){
					msg.what = STOPSPLASH;
					splashHandler.sendMessage(msg);
				}//end try
			}
		}.start();
	}
}
