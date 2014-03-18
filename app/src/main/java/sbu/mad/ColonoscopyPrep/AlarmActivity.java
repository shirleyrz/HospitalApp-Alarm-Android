package sbu.mad.ColonoscopyPrep;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import android.net.Uri;

public class AlarmActivity extends ActionBarActivity {

    final Context context = this;
    Button button;

    private MyCount timeCount;
    private TextView tvTimeLeft;
    private TextView tvDisplayDate;
    private Button btnChangeDate;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private Date curDate;
    private Date endDate;

    static final int DATE_DIALOG_ID = 9998;
    static final int TIME_DIALOG_ID = 9999;

    private TextView tvDisplayTime;
    private Button btnChangeTime;

    private NotificationManager nm;
    private PendingIntent pd;
    private String ALARM_TITLE = "ALARM";
    private String ALARM_CONTENT = "TIME IS UP!!";
    private String UPDATE_ALARM_TITLE = "UPDATED ALARM";
    private String UPDATE_ALARM_CONTENT = "TIME IS UP AGAIN!!";

    private int Notification_ID_ALARM = 8999;
    private Notification baseNF;
    private boolean flagUpdateAlarm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        init();
    }

    private void init() {
        setCurrentDateOnView();
        addListenerOnButton_countdown();
        addListenerOnButton_datepicker();
        addListenerOnButton_timerpicker();
        tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, AlarmActivity.class);
        pd = PendingIntent.getActivity(AlarmActivity.this, 0, intent, 0);
    }

    public void setCurrentDateOnView() {
        tvDisplayDate = (TextView) findViewById(R.id.tvDate);
        tvDisplayTime = (TextView) findViewById(R.id.tvTime);
        // dpResult = (DatePicker) findViewById(R.id.dpResult);
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        // set current date into textview
        tvDisplayDate.setText(new StringBuilder()
                // Month is 0 based, just add 1
                .append(month + 1).append("-").append(day).append("-")
                .append(year).append(" "));
        // set current time into textview
        tvDisplayTime.setText(
                new StringBuilder().append(pad(hour))
                        .append(":").append(pad(minute))
        );
    }


    public void addListenerOnButton_datepicker() {
        btnChangeDate = (Button) findViewById(R.id.btnChangeDate);
        btnChangeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
    }

     View.OnClickListener onClick_countDown = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            curDate = new Date(System.currentTimeMillis());
            endDate = new Date(System.currentTimeMillis());
            endDate.setDate(day);
            endDate.setMonth(month);
            endDate.setHours(hour);
            endDate.setMinutes(minute);
            endDate.setYear(year - 1900);
            setAlarm(curDate, endDate);
        }
    };

    public void addListenerOnButton_timerpicker() {
        btnChangeTime = (Button) findViewById(R.id.btnChangeTime);
        btnChangeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });
    }

    public void addListenerOnButton_countdown() {
        button = (Button) findViewById(R.id.btnCountDown);
        button.setOnClickListener(onClick_countDown);
    }

    private void setAlarm(Date startDate, Date endDate) {
        long diff = endDate.getTime() - startDate.getTime();
        if (diff < 0) {
            //TODO: SET A DIALOG
            tvTimeLeft.setText("Please set a correct time.");
        } else {
            timeCount = new MyCount(diff, 1000);
            timeCount.start();
        }
    }

    private void setAlarm(int hour, int min, int sec) {
        long waitingTime = (hour*60*60 + min*60 +sec)*1000;
        if (waitingTime < 0) {
            //TODO: SET A DIALOG
            tvTimeLeft.setText("Please set a correct time.");
        } else {
            timeCount = new MyCount(waitingTime, 1000);
            timeCount.start();
        }
    }

    private void generateBaseNotification(int ID, String title, String content) {
        baseNF = new Notification();
        baseNF.icon = R.drawable.icon;
        baseNF.tickerText = "This is a ALARM!";
        baseNF.defaults |= Notification.DEFAULT_SOUND;
        baseNF.defaults |= Notification.DEFAULT_VIBRATE;
        baseNF.defaults |= Notification.DEFAULT_LIGHTS;
        //infinite alarm
        baseNF.flags |= Notification.FLAG_INSISTENT;
        //define vibrate time
        long[] vir = {0,100,200,300};
        baseNF.vibrate = vir;
        //auto cancel after click the notification
        baseNF.flags |= Notification.FLAG_AUTO_CANCEL;
        //when click 'Clear', do not clear this notification
        //baseNF.flags |= Notification.FLAG_NO_CLEAR;
        baseNF.setLatestEventInfo(AlarmActivity.this, title, content, pd);
        //The first parameter is the unique ID for the Notification
        // and the second is the Notification object.
        nm.notify(ID, baseNF);
    }

    class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {

            if(flagUpdateAlarm){
                generateBaseNotification(Notification_ID_ALARM, UPDATE_ALARM_TITLE, UPDATE_ALARM_CONTENT);
            }else {
                generateBaseNotification(Notification_ID_ALARM, ALARM_TITLE, ALARM_CONTENT);
            }

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);
            alertDialogBuilder.setTitle("Time is UP!!");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Click yes to exit alarm")
                    .setCancelable(false)
                    .setNegativeButton("Remind me later", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, update the base alarm and close the dialog
                            //nm.notify(Notification_ID_ALARM, baseNF);
                            nm.cancel(Notification_ID_ALARM);
                            setAlarm(0,1,0);
                            flagUpdateAlarm = true;
                          //  generateBaseNotification(Notification_ID_ALARM, UPDATE_ALARM_TITLE, UPDATE_ALARM_CONTENT);
                          //  baseNF.setLatestEventInfo(AlarmActivity.this, UPDATE_ALARM_TITLE, UPDATE_ALARM_CONTENT, pd);
                          //  nm.notify(Notification_ID_ALARM, baseNF);
                            dialog.cancel();
                        }
                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, close
                    // current activity
                    nm.cancel(Notification_ID_ALARM);
                    dialog.cancel();
                    //AlarmActivity.this.finish();
                }
            });


            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long day = millisUntilFinished / (24 * 60 * 60 * 1000);
            long hour = (millisUntilFinished / (60 * 60 * 1000) - day * 24);
            long min = ((millisUntilFinished / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long sec = (millisUntilFinished / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
            tvTimeLeft.setText("Please wait for " + day + " days " + hour + ":" + min + ":" + sec + " ...");
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                // set date picker as current date
                return new DatePickerDialog(this, datePickerListener,
                        year, month, day);

            case TIME_DIALOG_ID:
                // set time picker as current time
                return new TimePickerDialog(this,
                        timePickerListener, hour, minute, false);

        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            year    = selectedYear;
            month   = selectedMonth;
            day     = selectedDay;

            // set selected date into textview
            tvDisplayDate.setText(new StringBuilder().append(month + 1)
                    .append("-").append(day).append("-").append(year)
                    .append(" "));

        }
    };

    private TimePickerDialog.OnTimeSetListener timePickerListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int selectedHour,
                                      int selectedMinute) {
                    hour = selectedHour;
                    minute = selectedMinute;

                    // set current time into textview
                    tvDisplayTime.setText(new StringBuilder().append(pad(hour))
                            .append(":").append(pad(minute)));
                }
            };


    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
