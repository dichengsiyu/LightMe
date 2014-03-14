package com.hellodev.lightme.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.hellodev.lightme.R;

public class SeekPreference extends Preference implements OnSeekBarChangeListener {
	private int MAX_PROGRESS;
	private int MIN_PROGRESS;
	private int PROGRESS_RATE;//seekbar分成越多份就滑动越流畅，如果每一格放得太大，就会出现事件调用不到的情况
	private int MAX_VALUE;
	private int MIN_VALUE;
	private int PROGRESS_INCREMENT;
	private SeekBar mSeekBar;
	private TextView mSummary;
	private int currentValue;
	
	public SeekPreference(Context context) {
		this(context, null);
	}

	public SeekPreference(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public SeekPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray mTypeArray = context.obtainStyledAttributes(attrs,
				R.styleable.SeekPreference);

		MAX_PROGRESS = mTypeArray.getInt(
				R.styleable.SeekPreference_maxProgress, 100);
		MIN_PROGRESS = mTypeArray.getInt(
				R.styleable.SeekPreference_minProgress, 0);
		PROGRESS_INCREMENT = mTypeArray.getInt(
				R.styleable.SeekPreference_progressIncrement, 1);
		PROGRESS_RATE = mTypeArray.getInt(
				R.styleable.SeekPreference_progressRate, 1);
		
		MAX_VALUE = MAX_PROGRESS / PROGRESS_RATE;
		MIN_VALUE = MIN_PROGRESS / PROGRESS_RATE;
		currentValue = MIN_VALUE;
		
		mTypeArray.recycle();
	}
	
	@Override
    protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
        try {
        	currentValue = restoreValue ? getPersistedInt(MIN_VALUE) : MIN_VALUE;
        } catch (NumberFormatException ex) {
        	currentValue = MIN_VALUE;
        }
    }

	@Override
	protected View onCreateView(ViewGroup parent) {
		return LayoutInflater.from(getContext()).inflate(getLayoutResource(), parent,
					false);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		
		mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
		mSeekBar.setMax(MAX_PROGRESS - MIN_PROGRESS);
		mSeekBar.setKeyProgressIncrement(PROGRESS_INCREMENT);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSummary = (TextView) view.findViewById(R.id.summary);
		mSeekBar.setProgress(currentValue * PROGRESS_RATE - MIN_PROGRESS);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int value,
			boolean fromUser) {
		currentValue = (value + MIN_PROGRESS) / PROGRESS_RATE;
		mSummary.setText(currentValue+"分钟");
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		persistInt(currentValue);
		notifyChanged();
		callChangeListener(currentValue);
	}
}
