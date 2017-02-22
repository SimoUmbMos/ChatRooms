package com.sorar.unichat.Dialogs;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sorar.unichat.R;


public class DeleteThreadDialog extends Dialog implements View.OnClickListener {
    private final Activity activity;
    private EditText ControllET;
    private String threadName;

    public DeleteThreadDialog(Activity activity, String threadName) {
        super(activity);
        this.activity=activity;
        this.threadName=threadName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delete_thread_dialog);
        Button next = (Button) findViewById(R.id.btn_dialog_next);
        Button cancel = (Button) findViewById(R.id.btn_dialog_cancel);
        TextView title=(TextView) findViewById(R.id.text_dialog);
        title.setText("Enter \"Sure\" below to Delete the Thread:"+threadName);
        next.setOnClickListener(this);
        cancel.setOnClickListener(this);
        ControllET = (EditText) findViewById(R.id.et_thread_name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dialog_next:
                String Sure = ControllET.getText().toString().toLowerCase();
                if (Sure.equals("sure")) {
                        DeleteThread(threadName);
                } else {
                    Toast.makeText(activity, "You don't press Sure", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_dialog_cancel:
                dismiss();
                break;
        }
    }

    /**
     * delete the chat room on firebase
    **/
    private void DeleteThread(final String ThreadName){
        /*
        * Make the vaule on the chat room null
        */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef=database.getReference().child("Chat Rooms").child(ThreadName);
        myRef.setValue(null);
        Toast.makeText(activity,"Thread Deleted", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
