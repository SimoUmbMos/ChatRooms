package com.sorar.unichat.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sorar.unichat.Dialogs.CreateThreadDialog;
import com.sorar.unichat.Dialogs.DeleteThreadDialog;
import com.sorar.unichat.Dialogs.PasswordThreadDialog;
import com.sorar.unichat.R;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainBoard";
    private Toolbar.LayoutParams lineLayoutParams,ThreadLayoutParams;
    private String userName;
    private String userId;
    private GoogleApiClient mGoogleApiClient;
    private long timePassed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lineLayoutParams=new Toolbar.LayoutParams(Toolbar.LayoutParams.FILL_PARENT,3);
        ThreadLayoutParams=
        new Toolbar.LayoutParams(Toolbar.LayoutParams.FILL_PARENT,Toolbar.LayoutParams.WRAP_CONTENT);
        SetUpGoogle();
        timePassed=0;
        Intent intent = getIntent();
        userName=intent.getStringExtra("userName");
        userId=intent.getStringExtra("userId");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(userName);
        toolbar.setSubtitle("");
        setUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout_menu:
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if(status.isSuccess()){
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent=new Intent(MainActivity.this,
                                            FullscreenActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Log.d(TAG, "Logout" );
                                    startActivity(intent);
                                    finish();
                                }
                        }
                    });
                return true;
            case R.id.create_menu:
                CreateThreadDialog ctd=new CreateThreadDialog(MainActivity.this,userName,userId);
                ctd.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * setup google account for logout
     **/
    private void SetUpGoogle(){
        /*
        * it login again with the google acount soo it ready to logout with the firebase acount.
        */
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * setup Layout for list of chat room
     **/
    private void setUp(){
        /*
        * Create a Listener to fire base for changes on the Database
        * initialize the viewNo to 0
        * add a textView with the name of the chat room
        * it create lines on the start ,bottom and between the chat room names
        * call the createLinkToThread(TextView Tag,String thread,String Password,String Ui)
        * to manage the click on the text view
        * increase viewNo by 1
        */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef =database.getReference().child("Chat Rooms");
        final Context context=this;
        myRef.addValueEventListener(new ValueEventListener() {
            int viewNo =0;
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String Password,Usercreator,UserUicreator,ThreadName,Status;
                TextView threadTextView;
                View line;
                LinearLayout layout = (LinearLayout) findViewById(R.id.content_main);
                layout.removeAllViews();
                line=new View(context);
                line.setLayoutParams(lineLayoutParams);
                line.setBackgroundColor(Color.parseColor("#000000"));
                layout.addView(line);
                for (DataSnapshot thread: snapshot.getChildren()) {
                        threadTextView= new TextView(context);
                        threadTextView.setClickable(true);
                        threadTextView.setLayoutParams(ThreadLayoutParams);
                        ThreadName=thread.getKey();
                        Usercreator=thread.child("UsernameCreator").getValue(String.class);
                        UserUicreator=thread.child("UidCreator").getValue(String.class);
                        if(thread.child("Password").exists()) {
                            Status="Private";
                            Password=thread.child("Password").getValue(String.class);
                        }else{
                            Status="Public";
                            Password="";
                        }
                        threadTextView.setText(Status+"\n"+ThreadName+"\nby:"+Usercreator);
                        threadTextView.setId(1+ viewNo);
                        createLinkToThread(threadTextView,ThreadName,Password,UserUicreator);
                        layout.addView(threadTextView);
                        line=new View(context);
                        line.setLayoutParams(lineLayoutParams);
                        line.setBackgroundColor(Color.parseColor("#000000"));
                        layout.addView(line);
                        viewNo++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LinearLayout layout = (LinearLayout) findViewById(R.id.content_main);
                layout.removeAllViews();
                String error="Error Code:\n" + Integer.toString(databaseError.getCode())+
                        "\n\nMessage:\n"+databaseError.getMessage()
                        +"\n\nDetails:\n"+databaseError.getDetails();
                TextView errorTextView= new TextView(context);
                errorTextView.setLayoutParams(new Toolbar.LayoutParams(
                        Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT));
                errorTextView.setText(error);
                layout.addView(errorTextView);
                errorTextView.setId(1+viewNo);
            }
        });
    }

    /**
     * manage the click on the chat room name
     **/
    private void createLinkToThread(TextView Tag,String thread,String Password,String Ui){
        /*
        * Take a text view
        * Check if is private and save the state on a boolean
        * set a touch listener on text view
        * If the user is the same as the creator and the user keep pressed the name for 1sec
        * Then create a delete Chat Room Dialog.
        * Else advance to the Chat Room
        */
        final TextView Tv=Tag;
        final String threadName=thread;
        final String UiCreator=Ui;
        final String password=Password;
        final boolean isPrivate=!Password.equals("");
        Tag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v.getId()==Tv.getId())
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            timePassed = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_UP:
                                if(timePassed!=0){
                                    if (System.currentTimeMillis() - timePassed >= 1000
                                            && userId.equals(UiCreator)) {
                                        DeleteThreadDialog dtd=new
                                                DeleteThreadDialog(MainActivity.this,threadName);
                                        dtd.show();
                                    }else{
                                        goToChatRoom(threadName,isPrivate,password);
                                    }
                                }else{
                                    goToChatRoom(threadName,isPrivate,password);
                                }
                        break;
                    }
                return true;
            }
        });
    }

    /**
     * go from main to chat room
     **/
    private void goToChatRoom(String threadName,boolean isPrivate, String password){
        /*
        * Check if chat room is private and it create a password thread dialog
        * if is private a dialog will pop up waiting for the password
        * else it call the method goToChatRoom() of the password dialog
        */
        if(isPrivate) {
            PasswordThreadDialog ptd=new PasswordThreadDialog(MainActivity.this,userName,userId,
                    threadName,password);
            ptd.show();
        }else{
            PasswordThreadDialog ptd=new PasswordThreadDialog(MainActivity.this,userName,userId,
                    threadName);
            ptd.goToChatRoom();
        }
    }
}
