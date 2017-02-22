package com.sorar.unichat.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sorar.unichat.R;



public class CreateThreadDialog extends Dialog implements View.OnClickListener {
    private final Activity activity;
    private EditText threadname,password;
    private CheckBox threadPrivate;
    private String Username,Uid;

    public CreateThreadDialog(Activity activity,String Username,String Uid) {
        super(activity);
        this.activity=activity;
        this.Username=Username;
        this.Uid=Uid;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.register_thread_dialog);
        Button next = (Button) findViewById(R.id.btn_dialog_next);
        Button cancel = (Button) findViewById(R.id.btn_dialog_cancel);
        next.setOnClickListener(this);
        cancel.setOnClickListener(this);
        threadname = (EditText) findViewById(R.id.et_thread_name);
        password = (EditText) findViewById(R.id.et_thread_password);
        threadPrivate = (CheckBox) findViewById(R.id.cb_private);
        threadPrivate.setOnClickListener(this);
        password.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_dialog_next:
                String threadName=threadname.getText().toString();
                if(!threadName.isEmpty()){
                    if(threadPrivate.isChecked()){
                        String threadPassword=password.getText().toString();
                        if(threadPassword.length()>0 && !threadPassword.isEmpty()){
                            createNewThread(Username,Uid,threadPassword,threadName);
                        }else{
                            Toast.makeText(activity,"Password must have 1 or more character", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        createNewThread(Username,Uid,"",threadName);
                    }
                }else{
                    Toast.makeText(activity,"You don't set the name of the Thread", Toast.LENGTH_LONG).show();
                }
            break;
            case R.id.btn_dialog_cancel:
                dismiss();
            break;
            case R.id.cb_private:
                if(!threadPrivate.isChecked()){
                    password.setEnabled(false);
                }else{
                    password.setEnabled(true);
                }
                break;
        }
    }

    /**
     * create new chat room on firebase
    **/
    private void createNewThread(String Username,String Uid,String Password,String ThreadName){
        /*
        * Create new Chat room to Fire base,and it save the Username and UserID of the creator
        * if the chat room is private it saves the password
        */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef =database.getReference().child("Chat Rooms").child(ThreadName);
        myRef.child("UsernameCreator").setValue(Username);
        myRef.child("UidCreator").setValue(Uid);
        if(!Password.equals(""))
            myRef.child("Password").setValue(Password);
        Toast.makeText(activity,"Thread Created", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
