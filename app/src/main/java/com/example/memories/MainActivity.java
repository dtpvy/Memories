package com.example.memories;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    Button button;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    CollectionReference dbUser;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                    auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                auth = FirebaseAuth.getInstance();
                                login();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (ApiException e) {
                    Toast.makeText(MainActivity.this, "Failed to sign in: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        Database db = new Database();
        dbUser = db.getDbUser();

        button = (Button) findViewById(R.id.button_start);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, options);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(intent);
            }
        });

        if (auth.getCurrentUser() != null) {
            this.login();
        }
    }

    public void login() {
        FirebaseUser user = auth.getCurrentUser();
        if (!user.isEmailVerified()) {
            button.setText("Confirm verified email");
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(MainActivity.this, "Please check your email to verify account!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            this.onBoarding();
        }
    }

    public void onBoarding() {
        FirebaseUser user = auth.getCurrentUser();

        dbUser.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Boolean onBoarding = false;
                if (!documentSnapshot.exists()) {
                    User newUser = new User(user.getUid(), user.getDisplayName(), user.getEmail());
                    dbUser.document(newUser.getId()).set(newUser);
                    onBoarding = newUser.getOnBoarding();
                    newUser.saveUser(MainActivity.this);
                } else {
                    User user = documentSnapshot.toObject(User.class);
                    onBoarding = user.getOnBoarding();
                    user.saveUser(MainActivity.this);
                }
                Toast.makeText(MainActivity.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();

                Intent intent;
                if (onBoarding == false) {
                    intent = new Intent(MainActivity.this, OnBoardingActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, HomeActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        });
    }
}