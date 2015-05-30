package org.breizhcamp.bzhtime;

import org.breizhcamp.bzhtime.dto.Proposal;
import org.breizhcamp.bzhtime.events.CountdownMgtEvt;
import org.breizhcamp.bzhtime.events.CurrentSessionEvt;
import org.breizhcamp.bzhtime.events.FlushScheduleCacheEvt;
import org.breizhcamp.bzhtime.events.TimeEvent;
import org.breizhcamp.bzhtime.util.FullScreenActivity;
import org.breizhcamp.bzhtime.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RemainingTimeActivity extends FullScreenActivity {

    @InjectView(R.id.fullscreen_content)
    protected TextView minutesTxt;

    @InjectView(R.id.fullscreen_content_controls)
    protected LinearLayout controls;

    @InjectViews({ R.id.overrideTimeBtn, R.id.changeRoomBtn })
    protected List<View> buttons;

    @InjectView(R.id.sessionNameTxt)
    protected TextView sessionNameTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_remaining_time);
        ButterKnife.inject(this);

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
        minutesTxt.setText(event.getNbMinutes() + " min");
    }

    public void onEventMainThread(CurrentSessionEvt event) {
        Proposal proposal = event.getProposal();
        if (proposal == null) {
            sessionNameTxt.setText(R.string.no_session);
        } else {
            sessionNameTxt.setText(proposal.getTitle());
        }
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
        final TextView scheduleUrl = ButterKnife.findById(dialogView, R.id.scheduleUrlTxt);
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
        //TODO
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

    protected String getRoomName() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getString("room", RemainingTimeApp.DEFAULT_ROOM);
    }

    protected void setRoomName(String roomName) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("room", roomName);
        editor.apply();
        updateTitle(roomName);
    }

    protected String getScheduleUrl() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getString("scheduleUrl", RemainingTimeApp.DEFAULT_ROOM);
    }

    protected void setScheduleUrl(String scheduleUrl) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("scheduleUrl", scheduleUrl);
        editor.apply();
    }

    private void updateTitle(String roomName) {
        setTitle("BzhTime - " + roomName);
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
