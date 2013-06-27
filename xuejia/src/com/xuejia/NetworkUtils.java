package com.xuejia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

public class NetworkUtils {
	private final static String tag="NetworkUtils";
	private final static int BUFFER = 1024;
	public static final    String CTWAP = "ctwap"; 
	public static final    String CMWAP = "cmwap"; 
	public static final    String WAP_3G = "3gwap"; 
	public static final    String UNIWAP = "uniwap"; 
	public static final    int TYPE_NET_WORK_DISABLED = 0;// 网络不可用 
	public static final    int TYPE_CM_CU_WAP = 4;// 移动联通wap10.0.0.172 
	public static final    int TYPE_CT_WAP = 5;// 电信wap 10.0.0.200 
	public static final    int TYPE_OTHER_NET = 6;// 电信,移动,联通,wifi 等net网络 
	public static final    int TYPE_WIFI = 7;// 电信,移动,联通,wifi 等net网络 
	public static Uri PREFERRED_APN_URI = Uri 
			.parse("content://telephony/carriers/preferapn"); 
	public static int checkNetworkType(Context mContext) { 
		try { 
			final ConnectivityManager connectivityManager = (ConnectivityManager) mContext 
					.getSystemService(Context.CONNECTIVITY_SERVICE); 
			final NetworkInfo mobNetInfoActivity = connectivityManager 
					.getActiveNetworkInfo(); 
			if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) { 
				// 注意一： 
				// NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络， 
				// 但是有些电信机器，仍可以正常联网， 
				// 所以当成net网络处理依然尝试连接网络。 
				// （然后在socket中捕捉异常，进行二次判断与用户提示）。 
				return TYPE_OTHER_NET; 
			} else { 
				// NetworkInfo不为null开始判断是网络类型 
				int netType = mobNetInfoActivity.getType(); 
				if (netType == ConnectivityManager.TYPE_WIFI) { 
					// wifi net处理 
					return TYPE_WIFI; 
				} else if (netType == ConnectivityManager.TYPE_MOBILE) { 
					// 注意二： 
					// 判断是否电信wap: 
					//不要通过getExtraInfo获取接入点名称来判断类型， 
					// 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null， 
					// 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码, 
					// 所以可以通过这个进行判断！ 
					final Cursor c = mContext.getContentResolver().query( 
							PREFERRED_APN_URI, null, null, null, null); 
					if (c != null) { 
						c.moveToFirst(); 
						final String user = c.getString(c 
								.getColumnIndex("user")); 
						if (!TextUtils.isEmpty(user)) { 
							if (user.startsWith(CTWAP)) { 
								//								Log.i("", "=====================>电信wap网络"); 
								return TYPE_CT_WAP; 
							} 
						} 
					} 
					c.close(); 
					// 注意三： 
					// 判断是移动联通wap: 
					// 其实还有一种方法通过getString(c.getColumnIndex("proxy")获取代理ip 
					//来判断接入点，10.0.0.172就是移动联通wap，10.0.0.200就是电信wap，但在 
					//实际开发中并不是所有机器都能获取到接入点代理信息，例如魅族M9 （2.2）等... 
					// 所以采用getExtraInfo获取接入点名字进行判断 
					String netMode = mobNetInfoActivity.getExtraInfo(); 
					//					Log.i("", "netMode ================== " + netMode); 
					if (netMode != null) { 
						// 通过apn名称判断是否是联通和移动wap 
						netMode=netMode.toLowerCase(); 
						if (netMode.equals(CMWAP) || netMode.equals(WAP_3G) 
								|| netMode.equals(UNIWAP)) { 
							//							Log.i("", "=====================>移动联通wap网络"); 
							return TYPE_CM_CU_WAP; 
						} 
					} 
				} 
			} 
		} catch (Exception ex) { 
			ex.printStackTrace(); 
			return TYPE_OTHER_NET; 
		} 
		return TYPE_OTHER_NET; 
	} 
	public static Object mLock=new Object(); 
	public static HttpHost getProxy(int networktype){
		HttpHost proxy =null;
		switch(networktype){
		case TYPE_CM_CU_WAP:
			proxy =new HttpHost("10.0.0.172", 80, "http");
			break;
		case TYPE_CT_WAP:
			proxy =new HttpHost("10.0.0.200", 80, "http");
			break;
		default:
			break;
		}
		return proxy;
	}
	public static HttpResponse getHttpResponse(Context ctx, String url) {

		int networktype=checkNetworkType(ctx);
		try{
			url=url.replace(" ", "%20");			
			switch(networktype){
			case TYPE_NET_WORK_DISABLED:
				return null;
			case TYPE_CM_CU_WAP:
			case TYPE_CT_WAP:{
				HttpHost proxy = getProxy(networktype);
				Uri uri=Uri.parse(url);
				HttpHost target = new HttpHost(uri.getHost(), 80, "http");
				DefaultHttpClient httpclient = new DefaultHttpClient();
				//尝试一次连接，防止CMWAP返回非所需页面
				String testUrl="http://m.baidu.com";
				HttpGet req_tmp = new HttpGet(testUrl);
				req_tmp.addHeader("User-Agent","xhnews");
				req_tmp.addHeader("Connection","close");
				httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
				HttpResponse response_tmp = httpclient.execute(target, req_tmp);
				if(response_tmp!=null){
					HttpEntity entity = response_tmp.getEntity(); 
					response_tmp=null;
					entity.consumeContent();
				}else{
				}
				HttpGet req = new HttpGet(url);
				req.addHeader("User-Agent","xhnews");
				req.addHeader("Connection","close");
				httpclient.getParams().setParameter(
						ConnRoutePNames.DEFAULT_PROXY, proxy);
				HttpResponse response = httpclient.execute(target, req);
				if(response!=null){
					int status_code=response.getStatusLine().getStatusCode();
					if(status_code!=200){
						response.getEntity().consumeContent();
						response=null;
					}

				}else{
				}
				return response;
			}//case TYPE_CT_WAP
			case TYPE_OTHER_NET:
			case TYPE_WIFI:{
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response = client.execute(get);
				return response;
			}//case TYPE_OTHER_NET:
			}//		switch	
		}catch(Exception e){
			return null;
		}
		return null;
	}	

	public static boolean isWiFiActive(Context inContext) {
		Context context = inContext.getApplicationContext();
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.isWifiEnabled();
	}

	public static String getActiveNetworkName(Context inContext) {
		Context context = inContext.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return "none";
		}
		NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
		if (activeNetwork == null) {
			return "none";
		}
		if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
			return "WiFi:" + activeNetwork.getExtraInfo();
		} else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
			return "Mobile:" + activeNetwork.getExtraInfo();
		} else {
			return activeNetwork.getTypeName() + ":"
					+ activeNetwork.getExtraInfo();
		}
	}

	public static String getHtmlResponse(Context context,String url) {
		HttpResponse response;
		InputStream is_response;
		try {
			response =getHttpResponse(context,url); //client.execute(httpGet); 
			if (response!=null&&response.getStatusLine().getStatusCode() == 200) {				
				HttpEntity entity = response.getEntity();
				is_response = entity.getContent();
				String res = convertStreamToString(is_response);		
				return res;
			} else {

				return null;
			}
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	public static InputStream getHtmlInputStream(Context context,String url){
		try {   
			HttpResponse response=getHttpResponse(context,url);
			if(response!=null){
				HttpEntity entity = response.getEntity(); 
				InputStream is = entity.getContent();
				return is;
			}else{
				return null;
			}
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	public static boolean downloadAndSave(Context context,String url, String savePath) {
		try {   
			HttpResponse response=getHttpResponse(context,url);
			if(response!=null){
				HttpEntity entity = response.getEntity(); 
				InputStream is = entity.getContent();
				String tmpFile=savePath+getTimeTmpStr()+".tmp";
				File file=new File(tmpFile);
				file.mkdirs();
				if(file.exists()){
					file.delete();
				}
				FileOutputStream out = new FileOutputStream(file);   

				byte[] b = new byte[BUFFER];   
				int len = 0;   
				while((len=is.read(b))!= -1){   
					out.write(b,0,len);   
				}   
				is.close();   
				out.close();
				File attachFile=new File(savePath);
				file.renameTo(attachFile);
				return true;
			}else{
				return false;
			}
		} catch (ClientProtocolException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
	public static String convertStreamToString(InputStream is) {
		if (is == null)
			return "";
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			//e.printStackTrace();;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				//e.printStackTrace();;
			}
		}
		return sb.toString();
	}	
	public static String getTimeTmpStr(){
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		SimpleDateFormat DateFormat = new SimpleDateFormat(
				"yyyyMMdd_HHmmss");
		String timeStr=DateFormat.format(date);
		return timeStr;
	}
	public static String getFileName(String url) {
		String filename = "";
		boolean isok = false;
		// 从UrlConnection中获取文件名称
		try {
			URL myURL = new URL(url);

			URLConnection conn = myURL.openConnection();
			if (conn == null) {
				return null;
			}
			Map<String, List<String>> hf = conn.getHeaderFields();
			if (hf == null) {
				return null;
			}
			Set<String> key = hf.keySet();
			if (key == null) {
				return null;
			}
			// Log.i("test", "getContentType:" + conn.getContentType() + ",Url:"
					// + conn.getURL().toString());
			for (String skey : key) {
				List<String> values = hf.get(skey);
				for (String value : values) {
					String result;
					try {
						result = new String(value.getBytes("ISO-8859-1"), "GBK");
						int location = result.indexOf("filename");
						if (location >= 0) {
							result = result.substring(location
									+ "filename".length());
							filename = result
									.substring(result.indexOf("=") + 1);
							isok = true;
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}// ISO-8859-1 UTF-8 gb2312
				}
				if (isok) {
					break;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 从路径中获取
		if (filename == null || "".equals(filename)) {
			filename = url.substring(url.lastIndexOf("/") + 1);
		}
		return filename;

	}
}
