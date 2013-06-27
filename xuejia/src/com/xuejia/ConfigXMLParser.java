package com.xuejia;

import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;
public class ConfigXMLParser {
	private static final String NODE_COLOR = "bgcolor";
	private static final String NODE_IMAGEURL = "bgimg";
	public static StartConfig parse(String xml)  {
		try
		{
			// ����Pull����
			XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser pullParser = pullParserFactory.newPullParser();
			// ����XML
			pullParser.setInput(new StringReader(xml));
			// ��ʼ
			int eventType = pullParser.getEventType(); 
			StartConfig sc = new StartConfig();        	 
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String nodeName = pullParser.getName();
				switch (eventType) {
				// �ĵ���ʼ
				case XmlPullParser.START_DOCUMENT:
					break;
					// �ڵ㿪ʼ	               
				case XmlPullParser.START_TAG:
					if (NODE_COLOR.equals(nodeName)) {
						sc.BackgroundColor=pullParser.nextText();
					} else if (NODE_IMAGEURL.equals(nodeName)) {
						sc.ImageURL=pullParser.nextText();
					}
					break;
					// �ڵ����
				case XmlPullParser.END_TAG:
					break;
				}
				eventType = pullParser.next();
			}//end while
			return sc;
		}catch(Exception e){
			Log.i("ex", e.toString());
			return null;	
		}
	}	
}