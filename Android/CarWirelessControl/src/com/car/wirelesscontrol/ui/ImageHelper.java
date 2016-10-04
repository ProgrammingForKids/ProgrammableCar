package com.car.wirelesscontrol.ui;

import com.car.wirelesscontrol.util.OpCode;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

class ImageHelper
{
	public View	view	= null;
	public int	index	= -1;

	public ImageHelper()
	{
	}

	public ImageHelper(View v, int index)
	{
		this.view = v;
		this.index = index;
	}

	void Ini()
	{
		this.view = null;
		this.index = -1;
	}

	ImageHelper Set(View v, int index)
	{
		this.view = v;
		this.index = index;
		return this;
	}

	ImageHelper Set(final ImageHelper other)
	{
		if (null == other)
		{
			Ini();
		}
		else
		{
			this.view = other.view;
			this.index = other.index;
		}
		return this;
	}

	int Opcode()
	{
		if (null != view)
		{
			return view.getId();
		}
		return -1;
	}

	void Select()
	{
		if (null != this.view)
		{
			this.view.setAlpha((float) 0.5);
			this.view.setBackgroundColor(Color.BLUE);
		}
	}

	public boolean IsActive()
	{
		return (null != view);
	}

	ImageHelper Unselect()
	{
		if (null != this.view)
		{
			this.view.setAlpha((float) 1.0);
			this.view.setBackgroundColor(Color.TRANSPARENT);
		}
		return this;
	}

	void SetImage(Context context, int recId)
	{
		if (null == view)
		{
			return;
		}
		if (null != context)
		{
			Drawable drawable = ContextCompat.getDrawable(context, recId);
			((ImageView) view).setImageDrawable(drawable);
		}
	}

	void RestoreImage(Context context)
	{
		if (null == view)
		{
			return;
		}
		if (null != context)
		{
			int drawableId = OpCode.DrawableId(view.getId());
			Drawable drawable = ContextCompat.getDrawable(context, drawableId);
			((ImageView) view).setImageDrawable(drawable);
		}
	}

	public static ImageView CreateImage(Context context, int opcode)
	{
		if (null == context)
		{
			return null;
		}
		int drawableId = OpCode.DrawableId(opcode);
		ImageView iv = new ImageView(context);
		Drawable drawable = ContextCompat.getDrawable(context, drawableId);

		iv.setImageDrawable(drawable);
		iv.setId(opcode);
		iv.setAdjustViewBounds(true);
		iv.setPadding(5, 5, 5, 5);
		iv.setScaleType(ScaleType.CENTER_INSIDE);
		return iv;
	}

	public static ImageView Store(Context context, int id, LinearLayout ll)
	{
		ImageView iv = CreateImage(context, id);
		if (null == iv)
		{
			return null;
		}
		iv.setPadding(5, 5, 5, 5);
		ll.addView(iv, 0);
		return iv;
	}

}// class Selected
