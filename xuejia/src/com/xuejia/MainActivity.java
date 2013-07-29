package com.xuejia;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.View.OnClickListener;
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

	private RelativeLayout mBtnHomeLayout;
	private RelativeLayout mBtnSearchLayout;
	private RelativeLayout mBtnCategoryLayout;
	private RelativeLayout mBtnShoppingCartLayout;
	private RelativeLayout mBtnAccountLayout;
	private ProgressDialog mWaitDialog;
	
	private int mState=0;
	//	private LinearLayout mToolbarLayout;
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
				.setMessage("�޷����ӷ����������������������ò�������������").setPositiveButton("ȷ��",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						finish();
					}
				}).show();
				break;
			case  OVERDUE:
				new AlertDialog.Builder(mThis).setTitle(mThis.getString(R.string.app_name))
				.setMessage("��ʾ�����Ѿ�����,�뵽����������ʽ��").setPositiveButton("ȷ��",
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
		setContentView(R.layout.activity_main);
		mThis=this;
		findViews();
		initActions();
		loadSplash();
		initWebView();
		getStartImage();
		//splash();
	}
	private void findViews(){
		mImageLayout=(LinearLayout) this.findViewById(R.id.layout_img);
		mStartupLayout=(RelativeLayout) this.findViewById(R.id.layout_splashscreen);
		mWebView=(WebView) this.findViewById(R.id.webview);
		mProgressBar=(ProgressBar) this.findViewById(R.id.progressBar_bottom);
		mProgressBar.setVisibility(View.GONE);
		mBtnHomeLayout=(RelativeLayout) this.findViewById(R.id.layout_home);
		mBtnSearchLayout=(RelativeLayout) this.findViewById(R.id.layout_search);
		mBtnCategoryLayout=(RelativeLayout) this.findViewById(R.id.layout_category);
		mBtnShoppingCartLayout=(RelativeLayout) this.findViewById(R.id.layout_shoppingcart);
		mBtnAccountLayout=(RelativeLayout) this.findViewById(R.id.layout_account);
		//		mToolbarLayout=(LinearLayout) this.findViewById(R.id.layout_bottombar);
		//		mToolbarLayout.setVisibility(View.GONE);
	}
	private void initActions(){
		mBtnHomeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url=getString(R.string.url_home);
				setToolbarBg(url);
				mWebView.loadUrl(url);
			}
		});
		mBtnSearchLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url=getString(R.string.url_search);
				setToolbarBg(url);
				mWebView.loadUrl(url);
			}
		});
		mBtnCategoryLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url=getString(R.string.url_category);
				setToolbarBg(url);
				mWebView.loadUrl(url);
			}
		});
		mBtnShoppingCartLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url=getString(R.string.url_shoppingcart);
				setToolbarBg(url);
				mWebView.loadUrl(url);
			}
		});
		mBtnAccountLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url=getString(R.string.url_user);
				setToolbarBg(url);
				mWebView.loadUrl(url);
			}
		});
	}
	private void loadSplash(){
		String colorStr=PreferencesManager.getBgColor(mThis);
		String img=PreferencesManager.getBgImage(mThis);
		if(img.equals(""))return;
		File imgFile=new File(img);
		if(imgFile.exists()){
			Drawable d=Drawable.createFromPath(img);
			mImageLayout.setBackgroundDrawable(d);
			int color=Color.parseColor(colorStr);
			mStartupLayout.setBackgroundColor(color);
		}
	}
	private void setToolbarBg(String url){
		url=url.toLowerCase();
		String prefixHome=getString(R.string.url_home).replace(".aspx","");
		//		String prefixCategory=getString(R.string.url_category).replace(".aspx","");
		//		String prefixSearch=getString(R.string.url_search).replace(".aspx","");
		//		String prefixShoppingCart=getString(R.string.url_shoppingcart).replace(".aspx","");
		//		String prefixAccount=getString(R.string.url_user).replace(".aspx","");
		mBtnHomeLayout.setBackgroundColor(Color.TRANSPARENT);
		mBtnSearchLayout.setBackgroundColor(Color.TRANSPARENT);
		mBtnCategoryLayout.setBackgroundColor(Color.TRANSPARENT);
		mBtnShoppingCartLayout.setBackgroundColor(Color.TRANSPARENT);
		mBtnAccountLayout.setBackgroundColor(Color.TRANSPARENT);
		//		if(!url.startsWith(getString(R.string.url_login))){
		//			mToolbarLayout.setVisibility(View.VISIBLE);		
		//		}		
		if(url.equals(prefixHome)){
			//��ҳ
			mBtnHomeLayout.setBackgroundResource(R.drawable.toolbar_selected);
		}else if(url.contains("/listproduct")){
			//����
			mBtnSearchLayout.setBackgroundResource(R.drawable.toolbar_selected);			
		}else  if(url.contains("/category")){
			//����
			mBtnCategoryLayout.setBackgroundResource(R.drawable.toolbar_selected);				
		}else if(url.contains("/shoppingcart")){
			//���ﳵ
			mBtnShoppingCartLayout.setBackgroundResource(R.drawable.toolbar_selected);	
		}else if(url.contains("/user")){
			//��Ա����
			mBtnAccountLayout.setBackgroundResource(R.drawable.toolbar_selected);				
		}
		//		if(url.equals(prefixHome)){
		//			//��ҳ
		//			mBtnHomeLayout.setBackgroundResource(R.drawable.toolbar_selected);
		//		}else if(url.startsWith(prefixSearch)){
		//			//����
		//			mBtnSearchLayout.setBackgroundResource(R.drawable.toolbar_selected);			
		//		}else if(url.startsWith(prefixCategory)){
		//			//����
		//			mBtnCategoryLayout.setBackgroundResource(R.drawable.toolbar_selected);				
		//		}else if(url.startsWith(prefixShoppingCart)){
		//			//���ﳵ
		//			mBtnShoppingCartLayout.setBackgroundResource(R.drawable.toolbar_selected);	
		//		}else if(url.startsWith(prefixAccount)){
		//			//��Ա����
		//			mBtnAccountLayout.setBackgroundResource(R.drawable.toolbar_selected);				
		//		}
	}
	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView(){
		mWebView.setHorizontalScrollBarEnabled(false);// ȡ��Horizontal ScrollBar��ʾ
		mWebView.setVerticalFadingEdgeEnabled(false);
		WebSettings bs = mWebView.getSettings();
		bs.setBuiltInZoomControls(false);
		bs.setSupportZoom(true);
		//����WebView���صķ���,	�����Ⱦ�����ȼ�
		bs.setRenderPriority(RenderPriority.HIGH); 	
		bs.setUseWideViewPort(false);
		bs.setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				String logoutURL=getString(R.string.url_logout);
				if(url.contains(logoutURL)){
					view.loadUrl(url);
					exit();
				}else{
					setToolbarBg(url);
					view.loadUrl(url);
				}
				return true;
			}
			public void onPageFinished(WebView view, String url) {
				mProgressBar.setVisibility(View.GONE);
//				if(mWaitDialog!=null)
//					mWaitDialog.dismiss();
				mState+=1;
				super.onPageFinished(view, url);
			}

			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mProgressBar.setVisibility(View.VISIBLE);
//				if(mState>0){
//					mWaitDialog=ProgressDialog.show(mThis,"ϵͳ��ʾ","���ڼ��أ����Ժ�......");
//				}
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl){
				mProgressBar.setVisibility(View.GONE);
//				if(mWaitDialog!=null)
//					mWaitDialog.dismiss();
				if(mWebView.canGoBack()){
					mWebView.goBack();
				}else{
					networkErr();
				}
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
		mBtnHomeLayout.setBackgroundResource(R.drawable.toolbar_selected);
		mWebView.loadUrl(mURL);
	}


	@Override
	public void onBackPressed(){
		if(mWebView.canGoBack()&&false){
			mWebView.goBack();
		}else{
			exit();
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK &&false) {
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
		new AlertDialog.Builder(this).setTitle("��ʾ")
		.setMessage("ȷ���˳�ѩ�ѳ��У�").setPositiveButton("ȷ��",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				finish();
			}
		}).setNegativeButton("ȡ��",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				// ȡ����ť�¼�
			}
		}).show();
	}
	private void networkErr(){
		new AlertDialog.Builder(this).setTitle("��ʾ")
		.setMessage("�޷����ӵ���������").setPositiveButton("�˳�",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				finish();
			}
		}).setNegativeButton("����",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				// ȡ����ť�¼�
				mWebView.reload();
			}
		}).show();
	}
	private void getStartImage(){
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					//					Calendar calendar = Calendar.getInstance();
					//					Date now = calendar.getTime();					
					//					SimpleDateFormat DateFormat = new SimpleDateFormat(
					//							"yyyy-MM-dd"); 
					//					Date overdueDt=DateFormat.parse("2013-07-25");
					//					if(now.after(overdueDt)){
					//						msg.what = OVERDUE;
					//						splashHandler.sendMessage(msg);
					//						return;
					//					}
					if(NetworkUtils.getActiveNetworkName(mThis)==null){
						//������
						msg.what = CONNECTION_FALIED;
						splashHandler.sendMessage(msg);
					}else{
						//����xml
						String startConfigURL=getString(R.string.url_start_config);
						String xml=NetworkUtils.getHtmlResponse(mThis, startConfigURL);
						if(xml!=null&&!xml.equals("")){
							msg.what = STOPSPLASH;
							splashHandler.sendMessage(msg);
							StartConfig sc=ConfigXMLParser.parse(xml);
							if(sc!=null){

								String url=sc.ImageURL;
								if(url.equals("")){
									PreferencesManager.setBgImage(mThis,"");
									PreferencesManager.setBgColor(mThis, "");
								}else{
									String fileName=NetworkUtils.getFileName(url);
									File root = Environment.getExternalStorageDirectory();
									File gbDir = new File(root, "/xuejia/");
									if (!gbDir.exists()) {
										gbDir.mkdir();
									}
									String savepath=gbDir.getAbsolutePath()+"/"+fileName;
									File imgFile=new File(savepath);
									if(!imgFile.exists()){
										//����
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
