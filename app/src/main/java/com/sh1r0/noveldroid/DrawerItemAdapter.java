package com.sh1r0.noveldroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerItemAdapter extends ArrayAdapter<DrawerItem> {
	private LayoutInflater inflater;

	public DrawerItemAdapter(Context context, int textViewResourceId, DrawerItem[] objects) {
		super(context, textViewResourceId, objects);
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DrawerItem drawerItem = this.getItem(position);
		DrawerItemHolder drawerItemHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
			TextView textView = (TextView) convertView.findViewById(R.id.drawer_item_text);
			ImageView iconView = (ImageView) convertView.findViewById(R.id.drawer_item_icon);

			drawerItemHolder = new DrawerItemHolder();
			drawerItemHolder.textView = textView;
			drawerItemHolder.iconView = iconView;

			convertView.setTag(drawerItemHolder);
		} else {
			drawerItemHolder = (DrawerItemHolder) convertView.getTag();
		}

		drawerItemHolder.textView.setText(drawerItem.getText());
		drawerItemHolder.iconView.setImageResource(drawerItem.getIcon());

		return convertView;
	}

	private static class DrawerItemHolder {
		private TextView textView;
		private ImageView iconView;
	}
}
