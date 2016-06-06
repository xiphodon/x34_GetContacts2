package com.gc.x34_getcontacts2;

import java.util.ArrayList;
import java.util.HashMap;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {

	private ListView lv_list;
	private LinearLayout ll_ProgressBar;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			// ���ؽ�����
			ll_ProgressBar.setVisibility(View.INVISIBLE);
			
			lv_list.setAdapter(new SimpleAdapter(MainActivity.this, contacts,
					R.layout.contact_list_item, new String[] { "name", "phone" },
					new int[] { R.id.tv_name, R.id.tv_phone }));
		};
	};
	
	
	private ArrayList<HashMap<String, String>> contacts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lv_list = (ListView) findViewById(R.id.lv_list);
		ll_ProgressBar = (LinearLayout) findViewById(R.id.ll_ProgressBar);
		
		new Thread(){
			public void run() {
				contacts = readContacts();
				handler.sendEmptyMessage(0);
			};
		}.start();

	}

	public ArrayList<HashMap<String, String>> readContacts() {
		// 1.��raw_contacts���ж�ȡ��ϵ�˵�id��"contact_id"��
		// 2.����contact_id��data���в�ѯ����Ӧ�ĵ绰�������ϵ������
		// 3.����mimetype������data��������������ϵ�˺ͺ�������

		// ͨ�������ṩ�߷�����ϵ�����ݿ�(��Ҫ��Ӷ�ȡ��ϵ��Ȩ��)
		ContentResolver cr = getContentResolver();
		// 1.��raw_contacts�ж�ȡ��ϵ�˵�id��"contact_id"��
		Cursor cursorContactId = cr.query(
				Uri.parse("content://com.android.contacts/raw_contacts"),
				new String[] { "contact_id" }, null, null, null);

		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		if (cursorContactId != null) {

			while (cursorContactId.moveToNext()) {
				String contactId = cursorContactId.getString(0);
				
				//ʵ�����ֻ���ѯ���� contactId �п�����null
				if(TextUtils.isEmpty(contactId)){
					continue;
				}

				// 2.����contact_id��data��(ʵ��������ͼ view_data)�в�ѯ����Ӧ�ĵ绰�������ϵ������
				Cursor cursorData = cr.query(
						Uri.parse("content://com.android.contacts/data"),
						new String[] { "data1", "mimetype" }, "contact_id = ?",
						new String[] { contactId }, null);

				if (cursorData != null) {

					HashMap<String, String> map = new HashMap<String, String>();
					while (cursorData.moveToNext()) {

						String data1 = cursorData.getString(0);
						//���data1 ����Ϊ�գ���ֱ��������һ��ѭ��
						if(TextUtils.isEmpty(data1)){
							continue;
						}
						
						String mimetype = cursorData.getString(1);
						
						if ("vnd.android.cursor.item/name".equals(mimetype)) {
							map.put("name", data1);
						} else if ("vnd.android.cursor.item/phone_v2"
								.equals(mimetype)) {
							map.put("phone", data1);
						}
						
					}

					// ��ÿ�����ݼ�������
					list.add(map);

					cursorData.close();
				}
			}
			cursorContactId.close();
		}
		return list;
	}

}
