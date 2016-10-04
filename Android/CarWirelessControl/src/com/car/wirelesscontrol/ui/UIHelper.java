package com.car.wirelesscontrol.ui;

import com.car.programmator.ui.R;
import com.car.wirelesscontrol.util.OpCode;
import android.app.Activity;
import android.content.ClipData;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Pair;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UIHelper implements Eraser.Callback
{
	private final boolean	bDragDrop		= true;

	private final int		SB_START		= R.drawable.start;
	private final int		SB_STOP			= R.drawable.stop;

	ImageHelper				_performed		= new ImageHelper();
	ImageHelper				_img_selected	= new ImageHelper();
	TextView				_prompt			= null;
	ImageView				_startBnt		= null;
	Eraser					_eraser			= null;
	LinearLayout			_tools_area		= null;
	LinearLayout			_current_area	= null;
	private FlowLayout		_command_area	= null;
	private boolean			_performMode	= false;

	final Activity			_activity;
	private final Chunkof	Chunk;

	public interface Callback
	{
		void onStartPerform();

		void onStopPerform();
	}

	private Callback	mCallback;
	private int			_command_areaId;
	private int			_tools_areaId;

	void PerformMode(boolean b)
	{
		_performMode = b;
	}

	boolean PerformMode()
	{
		return _performMode;
	}

	public UIHelper(Activity activity, Callback callback)
	{
		mCallback = callback;
		Chunk = new Chunkof();
		_activity = activity;
		_eraser = new Eraser(activity);
		_eraser.registerCallBack(this);
		_tools_area = (LinearLayout) activity.findViewById(R.id.area_tools);
		_tools_areaId = _tools_area.getId();
		_startBnt = (ImageView) activity.findViewById(R.id.image_tools);
		_startBnt.setId(SB_START);
		_startBnt.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int id = _startBnt.getId();
				switch (id)
				{
					case SB_START:
						mCallback.onStartPerform();
						break;
					case SB_STOP:
						mCallback.onStopPerform();
						break;
					default:
						break;
				}
			}
		});

		_command_area = (FlowLayout) activity.findViewById(R.id.commandArea);
		_command_areaId = _command_area.getId();

		_prompt = (TextView) activity.findViewById(R.id.prompt);
		_prompt.setVisibility(View.GONE);
		// if (_prompt.isShown())
		{
			// make TextView scrollable
			_prompt.setMovementMethod(new ScrollingMovementMethod());
			// clear prompt area if LongClick
			_prompt.setOnLongClickListener(new OnLongClickListener()
			{

				@Override
				public boolean onLongClick(View v)
				{
					_prompt.setText("");
					return true;
				}
			});
		}
		try
		{
			ImageHelper.Store(activity, OpCode._FORWARD, _tools_area).setOnTouchListener(new mTouchListener());
			ImageHelper.Store(activity, OpCode._BACK, _tools_area).setOnTouchListener(new mTouchListener());
			ImageHelper.Store(activity, OpCode._LEFT, _tools_area).setOnTouchListener(new mTouchListener());
			ImageHelper.Store(activity, OpCode._RIGHT, _tools_area).setOnTouchListener(new mTouchListener());
		}
		catch (NullPointerException e)
		{
		}
		_tools_area.setOnDragListener(new mDragListener());
		_command_area.setOnDragListener(new mDragListener());

	}

	public void SetPrompt(boolean b)
	{
		int vis = (b) ? View.VISIBLE : View.GONE;
		_prompt.setVisibility(vis);
	}

	public void SetPrompt(String txt)
	{
		if (_prompt.isShown())
		{
			String str = _prompt.getText().toString();
			_prompt.setText(str + txt);
		}

	}

	OnLongClickListener	myOnLongClickListener	= new OnLongClickListener()
												{

													@Override
													public boolean onLongClick(View v)
													{
														if (_performMode)
														{
															return true;
														}
														ClipData data = ClipData.newPlainText("", "");
														DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
														v.startDrag(data, shadowBuilder, v, 0);
														return true;
													}

												};

	private MenuItem	_showFileMenuItem;
	private String		_commandString;
	private int			_fileIconId;

	char OpcodeToDo()
	{
		return Select().OpcodeCurrent();
	}

	UIHelper Select()
	{
		if (!IsSelected())
		{
			if (-1 < _performed.index && _performed.index < _command_area.getChildCount())
			{
				_performed.view = _command_area.getChildAt(_performed.index);
				++_performed.index;
				_performed.Select();
			}
		}
		return this;
	}

	char OpcodeCurrent()
	{
		if (null != _performed)
		{
			return OpCode.OpcodeC(_performed.Opcode());
		}
		return 0;
	}

	void PreparationForStart()
	{
		_performed.RestoreImage(this._activity);
		_performed.index = 0;
	}

	ImageHelper Performed()
	{
		return _performed;
	}

	UIHelper Unselect()
	{
		if (IsPerformedValid())
		{
			_performed.Unselect();
		}
		return this;
	}

	boolean IsPerformedValid()
	{
		return (null != _performed);
	}

	void PerformError()
	{
		if (IsPerformedValid())
		{
			_performed.SetImage(this._activity, R.drawable.x);
		}
	}

	public void PerformObstacle()
	{
		if (IsPerformedValid())
		{
			_performed.SetImage(this._activity, R.drawable.o);
		}
	}

	public void StartBntToStop()
	{
		_startBnt.setId(SB_STOP);
		_startBnt.setImageDrawable(ContextCompat.getDrawable(this._activity, SB_STOP));
	}

	public void StartBntToStart()
	{
		_startBnt.setId(SB_START);
		_startBnt.setImageDrawable(ContextCompat.getDrawable(this._activity, SB_START));
	}

	boolean IsSelected()
	{
		return true;
	}

	// Command area
	public String CommandToString()
	{
		String ret = "";
		int count = _command_area.getChildCount();
		for (int k = 0; k < count; ++k)
		{
			View v = _command_area.getChildAt(k);
			char c = OpCode.OpcodeC(v.getId());
			ret += c;
		}
		return ret;
	}

	public void StringToCommand(String commands)
	{
		SaveCommandString(commands);
		_command_area.removeAllViews();
		int length = commands.length();
		for (int k = 0; k < length; ++k)
		{
			char c = commands.charAt(k);
			int id = OpCode.ImageId(c);
			if (0 != id)
			{
				ImageView iv = ImageToCommandArea(id);
				_command_area.addView(iv);
			}
		}
	}

	private ImageView ImageToCommandArea(int id)
	{
		ImageView iv = ImageHelper.CreateImage(_activity, id);
		if (null != iv)
		{
			// iv.setOnClickListener(_OnClickSelectInsert);
			iv.setOnLongClickListener(myOnLongClickListener);

		}
		return iv;
	}

	boolean IsCommandStringChanged()
	{
		String t = CommandToString();
		boolean ret = t.equals(_commandString);
		if (!ret)
		{
			_showFileMenuItem.setIcon(R.drawable.noload);
		}
		else
		{
			_showFileMenuItem.setIcon(_fileIconId);
		}

		return ret;
	}

	public void SaveCommandString(String string)
	{
		_commandString = string;
	}

	public void SetFileIcon(int iconId)
	{
		_fileIconId = iconId;
	}

	public void ShowFileIcon()
	{
		if (null != _showFileMenuItem)
		{
			_showFileMenuItem.setIcon(_fileIconId);
		}

	}

	public void SetMenuItem(MenuItem findItem)
	{
		_showFileMenuItem = findItem;
	}

	@Override
	public void onErase()
	{
		IsCommandStringChanged();
	}

	private final class mTouchListener implements OnTouchListener
	{

		public boolean onTouch(View view, MotionEvent motionEvent)
		{
			if (!bDragDrop)
			{
				return false;
			}
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
			{
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
				view.startDrag(data, shadowBuilder, view, 0);
				// view.setVisibility(View.INVISIBLE);
				return true;
			}
			else
			{
				return false;
			}
		}
	}// class MyTouchListener

	class mDragListener implements OnDragListener
	{
		@Override
		public boolean onDrag(View v, DragEvent event)
		{

			// Logger.Log.t(event.getAction(),(int) event.getX(), (int) event.getY());

			switch (event.getAction())
			{
				case DragEvent.ACTION_DRAG_LOCATION:
					if (_command_areaId == v.getId())
					{
						Pair<View, Integer> p = _command_area.GetIndex((int) event.getX(), (int) event.getY());
						_img_selected.Unselect();
						_img_selected.Set(p.first, p.second);
						_img_selected.Select();
					}
					break;
				case DragEvent.ACTION_DROP:

					// Dropped, reassign View to ViewGroup
					View view = (View) event.getLocalState();
					ViewGroup owner = (ViewGroup) view.getParent();
					if (_command_areaId == owner.getId())
					{
						owner.removeView(view);
						break;
					}
					if (_command_areaId == v.getId() && _tools_areaId == owner.getId())
					{
						_img_selected.Unselect();
						FlowLayout container = (FlowLayout) v;
						ImageView iv = ImageToCommandArea(view.getId());
						container.addView(iv, _img_selected.index);
					}
					break;
				case DragEvent.ACTION_DRAG_STARTED:
				case DragEvent.ACTION_DRAG_EXITED:
				case DragEvent.ACTION_DRAG_ENTERED:
				case DragEvent.ACTION_DRAG_ENDED:
				default:
					break;
			}
			return true;
		}

	}// class MyDragListener

	// ------------------------------------------------------------------
	// class Chunkof
	// ------------------------------------------------------------------
	public String GetFirstCommandChunk()
	{
		Chunk.Init();
		return Chunk.GetFirst();
	}

	public String GetNextCommandChunk()
	{
		return Chunk.GetNext();
	}

	static final int	YES		= 1;
	static final int	NO		= YES + 1;
	static final int	STOP	= YES + 2;

	public int AskNextCommandChunk()
	{
		Chunk.IncrementPosition();
		if (Chunk.IsStop())
		{
			return STOP;
		}
		if (Chunk.IsNeedNext())
		{
			return YES;
		}
		return NO;
	}

	class Chunkof
	{
		final int	MaxBufferSize	= 4;
		private int	_pos			= 0;
		private int	_curr			= 0;
		private int	_childCount		= 0;

		public Chunkof()
		{
		}

		void IncrementPosition()
		{
			_curr += 1;
		}

		boolean IsNeedNext()
		{
			if (0 == _pos - _curr)
			{
				return true;
			}
			return false;
		}

		boolean IsStop()
		{
			return _childCount == _curr;
		}

		void Init()
		{
			_curr = 0;
			_pos = 0;
			_childCount = _command_area.getChildCount();
		}

		String GetChunk(int start, int number)
		{

			String ret = "";
			if (_childCount <= start)
			{
				return ret;
			}
			int nCount = (start + number);
			if (nCount > _childCount)
			{
				nCount = _childCount;
			}

			for (int k = start; k < nCount; ++k)
			{
				View v = _command_area.getChildAt(k);
				char c = OpCode.OpcodeC(v.getId());
				ret += c;
			}
			_pos += ret.length();
			return ret;
		}

		String GetFirst()
		{
			return GetChunk(0, MaxBufferSize);
		}

		String GetNext()
		{
			if (_pos >= _childCount)
			{
				return "";
			}
			return GetChunk(_pos, MaxBufferSize - 1);
		}
	}// class Chunkof

}// class UIHelper
