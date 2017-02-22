package com.sorar.unichat.Dialogs;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sorar.unichat.Activity.MainActivity;
import com.sorar.unichat.R;
import com.sorar.unichat.Activity.ThreadMessageActivity;


public class PasswordThreadDialog extends Dialog implements View.OnClickListener {
    private final MainActivity activity;
    private final String password,threadName,userName,userId;
    private EditText etPassword;

    public PasswordThreadDialog(MainActivity activity,String userName,String userId, String threadName,
                                    String password) {
        super(activity);
        this.userName=userName;
        this.userId=userId;
        this.activity=activity;
        this.threadName=threadName;
        this.password=password;
    }

    public PasswordThreadDialog(MainActivity activity,String userName,String userId,String threadName) {
        super(activity);
        this.userName=userName;
        this.userId=userId;
        this.activity=activity;
        this.threadName=threadName;
        this.password="";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.password_thread_dialog);
        Button next = (Button) findViewById(R.id.btn_dialog_next);
        Button cancel = (Button) findViewById(R.id.btn_dialog_cancel);
        TextView title=(TextView) findViewById(R.id.text_dialog);
        title.setText("Enter the password for: "+threadName);
        next.setOnClickListener(this);
        cancel.setOnClickListener(this);
        etPassword = (EditText) findViewById(R.id.et_password);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dialog_next:
                String password = etPassword.getText().toString();
                if (password.equals(this.password)) {
                        goToChatRoom();
                } else {
                    Toast.makeText(activity, "Password Don't Match", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_dialog_cancel:
                dismiss();
                break;
        }

    }

    /**
     * go from Main to the chat room
     **/
    public void goToChatRoom(){
        /*
        * Go To ChatRoom Activity with intent passing threadName,userName,userId
        */
        Intent intent=new Intent(activity, ThreadMessageActivity.class);
        intent.putExtra("userName",userName);
        intent.putExtra("userId",userId);
        intent.putExtra("threadName",threadName);
        activity.startActivity(intent);
        dismiss();
    }
}
