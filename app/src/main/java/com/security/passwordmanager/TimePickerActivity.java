package com.security.passwordmanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import java.util.Calendar;

public class TimePickerActivity extends AppCompatActivity {

    private static final String ARG_TIME = "time";
    private static final String ARG_TITLE = "title";
    private static final String EXTRA_TIME = "extra_time";

    private TimePicker mTimePicker;

    @NonNull
    public static Intent newIntent(Context context, Calendar calendar, @StringRes int title) {
        Intent intent = new Intent(context, TimePickerActivity.class);
        intent.putExtra(ARG_TIME, calendar);
        intent.putExtra(ARG_TITLE, title);

        return intent;
    }

    public static Calendar parseIntent(Intent intent) {
        return (Calendar) intent.getSerializableExtra(EXTRA_TIME);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Support support = Support.get(this);
        @StyleRes int timePickerStyle;

        if (support.isLightTheme())
            timePickerStyle = R.style.Widget_MaterialComponents_TimePicker_Light;
        else
            timePickerStyle = R.style.Widget_MaterialComponents_TimePicker_Dark;

        Context newContext = new ContextThemeWrapper(getBaseContext(), timePickerStyle);

        setContentView(
                LayoutInflater
                .from(newContext)
                .inflate(R.layout.dialog_time, null)
        );

        Calendar calendar = (Calendar) getIntent().getSerializableExtra(ARG_TIME);
        int title = getIntent().getIntExtra(ARG_TITLE, R.string.account);
        setTitle(title);

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        mTimePicker = findViewById(R.id.dialog_time_picker);
        mTimePicker.setIs24HourView(true);
        mTimePicker.setHour(hours);
        mTimePicker.setMinute(minutes);
        mTimePicker.setBackgroundColor(support.getBackgroundColor());

        Button buttonCancel = findViewById(R.id.dialog_button_cancel);
        Button buttonOk = findViewById(R.id.dialog_button_ok);

        findViewById(R.id.constraint_layout_buttons).setBackgroundColor(support.getBackgroundColor());

        buttonCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        buttonOk.setOnClickListener(v -> {

            calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getHour());
            calendar.set(Calendar.MINUTE, mTimePicker.getMinute());

            Intent intent = new Intent();
            intent.putExtra(EXTRA_TIME, calendar);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//
//
//        return new AlertDialog.Builder(requireActivity())
//                .setView(v)
//                .setTitle(title)
//                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
//
//                    Date date1 = new GregorianCalendar(
//                            calendar.get(Calendar.YEAR),
//                            calendar.get(Calendar.MONTH),
//                            calendar.get(Calendar.DAY_OF_MONTH),
//                            mTimePicker.getHour(),
//                            mTimePicker.getMinute()
//                    ).getTime();
//
//                    sendResult(date1);
//                })
//                .create();
//    }

    public static class TimePickerActivityContract extends ActivityResultContract<Calendar, Calendar> {

        @StringRes
        private final int title;

        public TimePickerActivityContract(@StringRes int title) {
            this.title = title;
        }

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Calendar calendar) {
            return newIntent(context, calendar, title);
        }

        @Override
        public Calendar parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode != Activity.RESULT_OK || intent == null)
                return null;

            return parseIntent(intent);
        }
    }
}