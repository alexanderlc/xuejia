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
	public static final    int TYPE_NET_WORK_DISABLED = 0;// ���粻���� 
	public static final    int TYPE_CM_CU_WAP = 4;// �ƶ���ͨwap10.0.0.172 
	public static final    int TYPE_CT_WAP = 5;// ����wap 10.0.0.200 
	public static final    int TYPE_OTHER_NET = 6;// ����,�ƶ�,��ͨ,wifi ��net���� 
	public static final    int TYPE_WIFI = 7;// ����,�ƶ�,��ͨ,wifi ��net���� 
	public static Uri PREFERRED_APN_URI = Uri 
			.parse("content://telephony/carriers/preferapn"); 
	public static int checkNetworkType(Context mContext) { 
		try { 
			final ConnectivityManager connectivityManager = (ConnectivityManager) mContext 
					.getSystemService(Context.CONNECTIVITY_SERVICE); 
			final NetworkInfo mobNetInfoActivity = connectivityManager 
					.getActiveNetworkInfo(); 
			if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) { 
				// ע��һ�� 
				// NetworkInfo Ϊ�ջ��߲������õ�ʱ���������Ӧ���ǵ�ǰû�п������磬 
				// ������Щ���Ż������Կ������������� 
				// ���Ե���net���紦����Ȼ�����������硣 
				// ��Ȼ����socket�в�׽�쳣�����ж����ж����û���ʾ���� 
				return TYPE_OTHER_NET; 
			} else { 
				// NetworkInfo��Ϊnull��ʼ�ж����������� 
				int netType = mobNetInfoActivity.getType(); 
				if (netType == ConnectivityManager.TYPE_WIFI) { 
					// wifi net���� 
					return TYPE_WIFI; 
				} else if (netType == ConnectivityManager.TYPE_MOBILE) { 
					// ע����� 
					// �ж��Ƿ����wap: 
					//��Ҫͨ��getExtraInfo��ȡ������������ж����ͣ� 
					// ��Ϊͨ��Ŀǰ���Ŷ��ֻ��Ͳ��Է��ֽ�������ƴ�Ϊ#777����null�� 
					// ���Ż���wap�������Ҫ���ƶ���ͨwap����������һ���û���������, 
					// ���Կ���ͨ����������жϣ� 
					final Cursor c = mContext.getContentResolver().query( 
							PREFERRED_APN_URI, null, null, null, null); 
					if (c != null) { 
						c.moveToFirst(); 
						final String user = c.getString(c 
								.getColumnIndex("user")); 
						if (!TextUtils.isEmpty(user)) { 
							if (user.startsWith(CTWAP)) { 
								//								Log.i("", "=====================>����wap����"); 
								return TYPE_CT_WAP; 
							} 
						} 
					} 
					c.close(); 
					// ע������ 
					// �ж����ƶ���ͨwap: 
					// ��ʵ����һ�ַ���ͨ��getString(c.getColumnIndex("proxy")��ȡ����ip 
					//���жϽ���㣬10.0.0.172�����ƶ���ͨwap��10.0.0.200���ǵ���wap������ 
					//ʵ�ʿ����в��������л������ܻ�ȡ������������Ϣ����������M9 ��2.2����... 
					// ���Բ���getExtraInfo��ȡ��������ֽ����ж� 
					String netMode = mobNetInfoActivity.getExtraInfo(); 
					//					Log.i("", "netMode ================== " + netMode); 
					if (netMode != null) { 
						// ͨ��apn�����ж��Ƿ�����ͨ���ƶ�wap 
						netMode=netMode.toLowerCase(); 
						if (netMode.equals(CMWAP) || netMode.equals(WAP_3G) 
								|| netMode.equals(UNIWAP)) { 
							//							Log.i("", "=====================>�ƶ���ͨwap����"); 
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
				//����һ�����ӣ���ֹCMWAP���ط�����ҳ��
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
		// ��UrlConnection�л�ȡ�ļ�����
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
		// ��·���л�ȡ
		if (filename == null || "".equals(filename)) {
			filename = url.substring(url.lastIndexOf("/") + 1);
		}
		return filename;

	}
}
