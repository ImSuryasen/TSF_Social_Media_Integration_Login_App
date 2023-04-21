package com.example.tsf_socialmediaintegrationlogin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private TextView tv_login;

    private boolean tv_fb_login;
    private ImageView imageView;
    private TextView tv_Name;

    private boolean isGuest=false;

    private GoogleSignInClient mClient;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_login=findViewById(R.id.tv_login);
        tv_Name=findViewById(R.id.tv_Name);
        imageView=findViewById(R.id.img_View);
        mAuth=FirebaseAuth.getInstance();
        tv_login.setOnClickListener(v -> {

            if (isGuest){
                loginUser();
            }
            else logOutUser();

        });

        createRequest();


    }

    private void createRequest(){
        GoogleSignInOptions signInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mClient= GoogleSignIn.getClient(this,signInOptions);
    }

    private void loginUser(){
        Intent intent=mClient.getSignInIntent();
        activityResultLauncher.launch(intent);


    }

    ActivityResultLauncher<Intent> activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
       if(result.getResultCode()== Activity.RESULT_OK){
           Intent data=result.getData();

           Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
           try {
               GoogleSignInAccount account=task.getResult(ApiException.class);
               auth(account.getIdToken());

           } catch (ApiException e) {
               throw new RuntimeException(e);
           }

       }

    });

    private void auth(String token){
        AuthCredential credential= GoogleAuthProvider.getCredential(token, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this,task -> {
                   if (task.isSuccessful()){

                       Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                       userProfile();
                   }
                   else Toast.makeText(this, "Login Fail", Toast.LENGTH_SHORT).show();

                });

    }

    private void userProfile(){
        FirebaseUser user=mAuth.getCurrentUser();
        if(user != null){
            isGuest=false;
            tv_Name.setText(user.getDisplayName());
            Glide.with(this).load(user.getPhotoUrl()).into(imageView);
            tv_login.setText("LOG OUT");
            tv_fb_login=false;


        }else {
            tv_Name.setText("Guest");
            isGuest=true;
            tv_login.setText("SignIn with GOOGLE");

        }

    }


    private void logOutUser(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this,MainActivity.class));
        finish();

    }

    @Override
    protected void onStart() {
        super.onStart();
        userProfile();
    }
}