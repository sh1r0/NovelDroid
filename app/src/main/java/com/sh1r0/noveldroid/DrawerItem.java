package com.sh1r0.noveldroid;

import android.content.Context;

public class DrawerItem {
	private int id;
	private int text;
	private int icon;

	public static DrawerItem create(int id, int text, int icon, Context context) {
		DrawerItem item = new DrawerItem();
		item.setId(id);
		item.setText(text);
		item.setIcon(icon);
		return item;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getText() {
		return text;
	}

	public void setText(int text) {
		this.text = text;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}
}
