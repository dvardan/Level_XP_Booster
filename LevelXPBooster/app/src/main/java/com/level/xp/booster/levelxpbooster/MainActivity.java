package com.level.xp.booster.levelxpbooster;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import android.support.annotation.NonNull;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.nio.BufferUnderflowException;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener{
    Button pressButton;
    private int k;
    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mClient;
    private RewardedVideoAd mRewardedVideoAd;
    private GoogleApiClient mGoogleApiClient;

    private static final int RC_ACHIEVEMENT_UI = 9003;
    private static final int RC_LEADERBOARD_UI = 9004;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mClient = GoogleSignIn.getClient(MainActivity.this, gso);

        if(!isSignedIn()){
            signIn();




        }
        else{

        }





        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();


        Button ach = (Button)findViewById(R.id.ach);
        ach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                //Games.getAchievementsClient(MainActivity.this,acct).unlock(getString(R.string.achievement_5_click));

             /*   Nar urem@ es function@ kancheluc error a tali, chem jogum inch a
                bayc pti achievmentneri window baci
                ha u indz tvuma et client id maydii het a kapvac
                es functionn el amenatakn em decler arel*/
                mGoogleApiClient.connect();
                System.out.println("after connect");
                //showAchievements();
                showLeaderboard();

            }
        });








        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        int myIntValue = sp.getInt("points", 0);
        k = myIntValue;

        pressButton = (Button)findViewById(R.id.pressButton);
        if(k != 0){
            pressButton.setText(Integer.toString(k));
        }
        pressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                k++;
                pressButton.setText(Integer.toString(k));

            }
        });

        final AdView adView = (AdView)findViewById(R.id.adView);

        //Change to real banner
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("33BE2250B43518CCDA7DE426D04EE232").build();
        adView.loadAd(adRequest);


        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);

        loadRewardedVideoAd();

        ImageView splash = (ImageView)findViewById(R.id.bigSplash);
        splash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();

                }

            }
        });
        ImageView splash1 = (ImageView)findViewById(R.id.splash1);
        splash1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();

                }

            }
        });
        ImageView splash2 = (ImageView)findViewById(R.id.splash2);
        splash2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();

                }

            }
        });






    }



    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
    }


    @Override
    public void onRewardedVideoAdLoaded() {

        System.out.println("onRewardedVideoAdLoaded");

    }

    @Override
    public void onRewardedVideoAdOpened() {

        System.out.println("onRewardedVideoAdOpened");

    }

    @Override
    public void onRewardedVideoStarted() {
        System.out.println("onRewardedVideoStarted");

    }

    @Override
    public void onRewardedVideoAdClosed() {
        System.out.println("onRewardedVideoAdClosed");
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        System.out.println("onRewarded");
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
        k = k + 200;
        pressButton.setText(Integer.toString(k));



    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        System.out.println("onRewardedVideoAdLeftApplication");
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());


    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        System.out.println("onRewardedVideoAdFailedToLoad");
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("points", k);
        editor.commit();

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("points", k);
        editor.commit();

    }


    //// Methods For Sign In
    private void signIn() {
        Intent signInIntent = mClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.

        }
    }







    private void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {

                            GoogleSignInAccount signedInAccount = task.getResult();
                            Toast.makeText(MainActivity.this,signedInAccount.getEmail().toString(), Toast.LENGTH_LONG).show();

                        } else {
                            // Player will need to sign-in explicitly using via UI
                        }
                    }
                });
    }
    private boolean isSignedIn(){
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //signInSilently();
    }


/////////////////////////////////////////////////
    private void showAchievements() {
        System.out.println("start showAchievements");
        Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
            .getAchievementsIntent()
            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    System.out.println("in onSuccess");
                    startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                }
            });


}
    private void showLeaderboard() {
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getLeaderboardIntent(getString(R.string.leaderboard_lead))
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_LEADERBOARD_UI);
                    }
                });
    }



}


