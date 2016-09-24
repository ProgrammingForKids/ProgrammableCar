package com.car.wirelesscontrol.ui;

import com.car.programmator.ui.R;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

public class Eraser
{
	private final ImageView		_eraser;
	private final FlowLayout	_command_aria;

	public interface Callback
	{
		void onErase();
	}

	private Callback mCallback;

	public void registerCallBack(Callback callback)
	{
		mCallback = callback;
	}

	public Eraser(Activity activity)
	{
		_command_aria = (FlowLayout) activity.findViewById(R.id.commandArea);
		_eraser = (ImageView) activity.findViewById(R.id.image_eraser);
		_eraser.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				int index = _command_aria.getChildCount();
				if (0 < index)
				{
					_command_aria.removeViewAt(index - 1);
				}
				mCallback.onErase();
			}
		});
		_eraser.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View v)
			{
				_command_aria.removeAllViews();
				mCallback.onErase();
				return false;
			}
		});
	}

}// class Eraser
