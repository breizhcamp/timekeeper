package org.breizhcamp.bzhtime;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.breizhcamp.bzhtime.dto.Proposal;
import org.breizhcamp.bzhtime.events.CountdownMgtEvt;
import org.breizhcamp.bzhtime.events.CurrentSessionEvt;
import org.breizhcamp.bzhtime.events.FlushScheduleCacheEvt;
import org.breizhcamp.bzhtime.events.MsgEvt;
import org.breizhcamp.bzhtime.events.TimeEvent;
import org.breizhcamp.bzhtime.util.FullScreenActivity;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RemainingTimeActivity extends FullScreenActivity {

    @BindView(R.id.timeLayout)
    protected FrameLayout timeLayout;

    @BindView(R.id.fullscreen_content)
    protected TextView minutesTxt;

    @BindView(R.id.remainingProgressBar)
    protected ProgressBar secProgressBar;

    @BindView(R.id.fullscreen_content_controls)
    protected LinearLayout controls;

    @BindViews({R.id.overrideTimeBtn, R.id.changeRoomBtn})
    protected List<View> buttons;

    @BindView(R.id.overrideTimeBtn)
    protected Button overrideButton;

    @BindView(R.id.sessionNameTxt)
    protected TextView sessionNameTxt;

    /**
     * If override is defined, this variable is > 0
     */
    private int lastOverride;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_remaining_time);
        ButterKnife.bind(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_remaining_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        updateTitle(getRoomName());
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().post(new CountdownMgtEvt(false));
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCountdown();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /* ***********  EVENTS  ********** */
    public void onEventMainThread(TimeEvent event) {
        Period remaining = event.getRemaining();
        int minutes = remaining.toStandardMinutes().getMinutes();
        int seconds = remaining.getSeconds();

        if (minutes < 0) minutes = 0;

        minutesTxt.setText(Integer.toString(minutes));

        if (minutes == 0 && seconds <= 12 && event.isTimerRunning()) { // if within the last 12 seconds of the timer
            timeLayout.setBackgroundResource(R.drawable.times_up_animation);
            AnimationDrawable animation = (AnimationDrawable) timeLayout.getBackground();
            animation.start();
        } else if (minutes == 0 && !event.isTimerRunning()) {
            timeLayout.setBackgroundColor(getResources().getColor(R.color.stopped_bg));
        } else if (minutes <= 2) {
            timeLayout.setBackgroundColor(getResources().getColor(R.color.end_bg));
        } else if (minutes <= 5) {
            timeLayout.setBackgroundColor(getResources().getColor(R.color.warn_bg));
        } else {
            timeLayout.setBackgroundColor(getResources().getColor(R.color.normal_bg));
        }

        secProgressBar.setProgress(60 - seconds);

        if (lastOverride > 0 && minutes != lastOverride) {
            lastOverride = minutes;
            setOverrideTime(String.valueOf(minutes));
            if (minutes == 0) { // TODO à revoir
                overrideButton.setText(R.string.override_time);
            }
        }
    }

    public void onEventMainThread(CurrentSessionEvt event) {
        Proposal proposal = event.getProposal();
        if (proposal == null) {
            minutesTxt.setText(Integer.toString(0));

            Proposal next = event.getNext();
            if (next == null) {
                sessionNameTxt.setText(R.string.no_session);
            } else {
                final DateTimeFormatter formatter;
                if (next.getStartDate().toLocalDate().equals(LocalDate.now())) {
                    formatter = DateTimeFormat.forPattern("'à' HH'h'mm");
                } else {
                    formatter = DateTimeFormat.forPattern("'le' EE dd MMM yyyy 'à' HH'h'mm");
                }
                sessionNameTxt.setText(getString(R.string.no_session_with_next,
                        next.getName(), next.getStartDate().toString(formatter)));
            }
        } else {
            sessionNameTxt.setText(proposal.getName());
        }
    }

    public void onEventMainThread(MsgEvt event) {
        Toast.makeText(this, event.getMsg(), Toast.LENGTH_LONG).show();
    }

    /* ***********  ACTIONS  ********** */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload:
                reloadSchedule();
                return true;

            case R.id.action_options:
                changeScheduleUrl();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void changeScheduleUrl() {
        final View dialogView = getLayoutInflater().inflate(R.layout.options_dialog, null);
        final TextView scheduleUrl = dialogView.findViewById(R.id.scheduleUrlTxt);
        scheduleUrl.setText(getScheduleUrl());

        new AlertDialog.Builder(this)
                .setTitle(R.string.options)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setScheduleUrl(scheduleUrl.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @OnClick(R.id.overrideTimeBtn)
    protected void onOverrideTimeClick(View view) {
        if (lastOverride > 0) {
            //cancel override time
            lastOverride = 0;
            setOverrideTime("0");
            startCountdown();
            overrideButton.setText(R.string.override_time);
            return;
        }

        //define override time
        overrideButton.setText(R.string.override_time_cancel);
        final View dialogView = getLayoutInflater().inflate(R.layout.override_time_dialog, null);
        final TextView overrideTime = dialogView.findViewById(R.id.overrideTimeTxt);
        String savedOverrideTime = getOverrideTime();
        if (!savedOverrideTime.equals("0")) {
            overrideTime.setText(savedOverrideTime);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.override_time)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String time = overrideTime.getText().toString();
                        if (time.length() == 0) {
                            time = "0";
                        }
                        defineOverrideTime(time);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @OnClick(R.id.changeRoomBtn)
    protected void onChangeRoomClick(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.pick_room)
                .setItems(RemainingTimeApp.ROOMS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRoomName(RemainingTimeApp.ROOMS[which]);
                        startCountdown();
                    }
                })
                .show();
    }

    protected void startCountdown() {
        EventBus.getDefault().post(new CountdownMgtEvt(true, getRoomName()));
    }

    protected void reloadSchedule() {
        EventBus.getDefault().post(new FlushScheduleCacheEvt(getScheduleUrl()));
    }

    protected void defineOverrideTime(String minutes) {
        setOverrideTime(minutes);
        sessionNameTxt.setText("Temps manuel");

        int min = Integer.parseInt(minutes);
        lastOverride = min;
        EventBus.getDefault().post(new CountdownMgtEvt(true, min));
    }

    protected String getRoomName() {
        return getPref("room", RemainingTimeApp.DEFAULT_ROOM);
    }

    protected void setRoomName(String roomName) {
        setPref("room", roomName);
        updateTitle(roomName);
    }

    protected String getScheduleUrl() {
        return getPref("scheduleUrl", RemainingTimeApp.DEFAULT_SCHEDULE_URL);
    }

    protected void setScheduleUrl(String scheduleUrl) {
        setPref("scheduleUrl", scheduleUrl);
    }

    protected String getOverrideTime() {
        return getPref("overrideTime", "80");
    }

    protected void setOverrideTime(String overrideTime) {
        setPref("overrideTime", overrideTime);
    }

    private void updateTitle(String roomName) {
        setTitle("BzhTime - " + roomName);
    }

    private String getPref(String key, String defaultValue) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getString(key, defaultValue);
    }

    private void setPref(String key, String value) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    protected View getContent() {
        return minutesTxt;
    }

    @Override
    public LinearLayout getControls() {
        return controls;
    }

    @Override
    public List<View> getButtons() {
        return buttons;
    }
}
