package our.amazing.clock;

import android.content.Intent;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import our.amazing.clock.R;
import our.amazing.clock.alarms.Alarm;
import our.amazing.clock.ringtone.AlarmActivity;
import our.amazing.clock.util.ParcelableUtil;

import java.util.Locale;

import static our.amazing.clock.YouTubePlayer.EXTRA_RINGING_OBJECT;


public class Auth extends AppCompatActivity {
    private static final String TAG = "Auth";

    private static final int RC_SIGN_IN = 123;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton signInButton;
    Button playSongBtn;
    ImageView welcome;
    boolean islogedIn=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adjustColor();
        setContentView(R.layout.activity_auth);

        welcome = (ImageView) findViewById(R.id.imageViewWelcome);
        adjustLocale();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        mAuth = FirebaseAuth.getInstance();

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    islogedIn=true;
                }
                if(islogedIn){
                    Intent i = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(i);
                }
            }
        });

        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            islogedIn=true;
        }
        if(islogedIn){
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
        }

        signInButton = (SignInButton)findViewById(R.id.signInBtn);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            islogedIn = true;
        }
        if (islogedIn) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void adjustLocale(){
        Locale current = getResources().getConfiguration().locale;
        if(current.toString().contains("IL")){
            welcome.setImageResource(R.drawable.clockheb);
        }
        else{
            welcome.setImageResource(R.drawable.clockeng);
        }
    }


    private void adjustColor(){
        final String themeDark = getString(R.string.theme_dark);
        final String themeBlack = getString(R.string.theme_black);
        String theme = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.key_theme), null);
        if (themeDark.equals(theme)) {
            setTheme(R.style.AppTheme_Dark);
        } else if (themeBlack.equals(theme)) {
            setTheme(R.style.AppTheme_Black);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "succes");
                            FirebaseUser user = mAuth.getCurrentUser();
                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            if(isNew){ // for the first Time

//                                User newUser = new User(user.getEmail(),user.getDisplayName());
//                                //push to dataBase
//                                mDatabase= FirebaseDatabase.getInstance();
//                                DatabaseReference myRef = mDatabase.getReference();
//                                myRef.child("users").child(user.getUid()).setValue(newUser);
//                                //myRef.child("users").setValue(user.getUid());


                                updateUI(user);
                            }
                            else{
                                updateUI(user);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(),"failed",Toast.LENGTH_LONG);
                        }

                        // ...
                    }
                });
    }
    public void updateUI(FirebaseUser user)
    {
        if( user != null)
        {
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
        }
    }

}
