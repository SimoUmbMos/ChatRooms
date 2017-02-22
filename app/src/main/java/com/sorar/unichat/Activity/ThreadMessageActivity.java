package com.sorar.unichat.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sorar.unichat.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ThreadMessageActivity extends AppCompatActivity {
    private String userName,userId,threadName;
    private ScrollView sv;
    private Toolbar.LayoutParams lineLayoutParams,messageLayoutParams;
    private Context context;
    private DatabaseReference myRef;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_message);

        messageLayoutParams=new Toolbar.LayoutParams(Toolbar.LayoutParams.FILL_PARENT,
                Toolbar.LayoutParams.WRAP_CONTENT);
        lineLayoutParams=new Toolbar.LayoutParams(Toolbar.LayoutParams.FILL_PARENT,5);

        context=this;
        Intent intent = getIntent();
        userName=intent.getStringExtra("userName");
        userId = intent.getStringExtra("userId");
        threadName = intent.getStringExtra("threadName");
        layout = (LinearLayout) findViewById(R.id.content_thread_message);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef =database.getReference().child("Chat Rooms").child(threadName).child("Messages");
        sv =(ScrollView)findViewById(R.id.questionListSV);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(userName);
        toolbar.setSubtitle(threadName);
        ImageView SendButton=(ImageView)findViewById(R.id.iv_send);
        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    sendMessage();
            }
        });

        setUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back_to_main:
                finish();
                return true;
            case R.id.clean_thread:
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference Ref =database.getReference().child("Chat Rooms").child(threadName);
                Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String Uid=dataSnapshot.child("UidCreator").getValue(String.class);
                        if(Uid.equals(userId))
                            Ref.child("Messages").setValue(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * send message to firebase
    **/
    private void sendMessage() {
        /*
        *   Take the date and time and store it as string
        *   Take the text message from the edit text,check if the length is bigger than 0,and is not
        *   empty.
        *   Check If Chat Room exist.
        *   Create a new listener to fire base and save the UserId,UserName,Message and Time of Send
        *   load again setup();
        *   Set the Message Edit text to empty
        */
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("hh:mm aaa dd/MM/yyyyy");
        final String formattedDate = df.format(c.getTime());
        EditText etMessage = (EditText) findViewById(R.id.et_message);
        final String Message = etMessage.getText().toString();
        if (Message.length() > 0 && (!Message.isEmpty())) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference().child("Chat Rooms").child(threadName);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long i = 0;
                                if (dataSnapshot.exists())
                                    i = i + dataSnapshot.getChildrenCount();
                                DatabaseReference message = myRef.child("message"+Long.toString(i));
                                message.child("UserSender").setValue(userName);
                                message.child("DateSend").setValue(formattedDate);
                                message.child("MessageBody").setValue(Message);
                                message.child("UserId").setValue(userId);
                                setUp();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(context, "Error Sending Message", Toast.LENGTH_LONG)
                                        .show();
                            }
                      });
                    }else{
                        Toast.makeText(context, "Thread Deleted", Toast.LENGTH_LONG)
                                .show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Error Sending Message", Toast.LENGTH_LONG).show();
                }
            });
            etMessage.setText("");
        }
    }

    /**
     * setup Layout of messages
    **/
    private void setUp(){
        /*
        *   Create a new Listener of changes on fire base
        *   Clean up the layout
        *   Add Messages on layout with the method:
        *       loadMessage(viewNo,UserSender,DateSend,threadTextView,message);
        *   And add a little space on the messages
        */
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                DataSnapshot message;
                final int[] viewNo = {0};
                int i=0;
                boolean goOn=true;
                layout.removeAllViews();
                String UserSender,DateSend;
                final TextView[] threadTextView = new TextView[1];
                View line;
                while(goOn){
                    if(snapshot.child("message"+String.valueOf(i)).exists()){
                        message=snapshot.child("message"+String.valueOf(i));
                        UserSender=message.child("UserSender").getValue(String.class);
                        DateSend=message.child("DateSend").getValue(String.class);
                        loadMessage(viewNo,UserSender,DateSend,threadTextView,message);
                        line=new View(context);
                        line.setLayoutParams(lineLayoutParams);
                        layout.addView(line);
                        sv.post(new Runnable() {
                            @Override
                            public void run() {
                                sv.scrollTo(0,sv.getBottom());
                            }
                        });
                        viewNo[0]++;
                        i++;
                    }else{
                        goOn=false;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * load a message on layout
    **/
    private void loadMessage(int[] viewNo, String userSender, String dateSend,
                             TextView[] threadTextView, DataSnapshot message) {
        /*
        *   Check if in fire base exist the child "MessageBody"
        *   if exist:
        *       create a new text view.
        *       set the Message-dateOfSand-UserSender as text.
        *       add it in the layout.
        *   The viewNo is the number of the message(start with 0)
        */
        if(message.child("MessageBody").exists()) {
            String MessageBody = message.child("MessageBody").getValue(String.class);
            threadTextView[0] = new TextView(context);
            threadTextView[0].setClickable(false);
            threadTextView[0].setLayoutParams(messageLayoutParams);
            if (userSender.equals(userName)) {
                threadTextView[0].setText(MessageBody + "\nat " + dateSend);
                threadTextView[0].setGravity(Gravity.RIGHT);
            } else
                threadTextView[0].setText(MessageBody + "\nby:" + userSender + "\nat " + dateSend);
            threadTextView[0].setId(1 + viewNo[0]);
            layout.addView(threadTextView[0]);
        }
    }
}
