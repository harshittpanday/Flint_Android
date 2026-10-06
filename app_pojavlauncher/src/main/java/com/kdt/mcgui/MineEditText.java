package com.kdt.mcgui;

import android.content.*;
import android.util.*;
import android.widget.EditText;

public class MineEditText extends androidx.appcompat.widget.AppCompatEditText {
	public MineEditText(Context ctx) {
		super(ctx);
		init();
	}

	public MineEditText(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init();
	}

	public void init() {
		setBackgroundResource(net.kdt.pojavlaunch.R.drawable.flint_edit_text_background);
		setTextColor(getResources().getColor(net.kdt.pojavlaunch.R.color.primary_text));
		setHintTextColor(getResources().getColor(net.kdt.pojavlaunch.R.color.secondary_text));
		setPadding(5, 5, 5, 5);
	}
}
