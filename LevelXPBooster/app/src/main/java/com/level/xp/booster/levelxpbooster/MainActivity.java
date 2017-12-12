package com.level.xp.booster.levelxpbooster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
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
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import android.support.annotation.NonNull;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import android.app.AlertDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.games.Player;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener{
    protected Button pressButton;
    protected int score;
    protected RewardedVideoAd mRewardedVideoAd;
    // Client used to sign in with Google APIs
    protected GoogleSignInClient mGoogleSignInClient;
    // Client variables
    protected AchievementsClient mAchievementsClient;
    protected LeaderboardsClient mLeaderboardsClient;
    protected PlayersClient mPlayersClient;
    // request codes we use when invoking an external activity
    protected static final int RC_UNUSED = 5001;
    protected static final int RC_SIGN_IN = 9001;
    protected static final int RC_LEADERBOARD_UI = 9004;
    // tag for debug logging
    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)

    protected GoogleSignInAccount myAccount;
    private Toast toast;

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide the top Title bar
        getSupportActionBar().hide();
        //To Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Create the client used to sign in to Google services.
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        //Sign in
        signInSilently();
        signIn();

/*


        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                .setViewForPopups(findViewById(android.R.id.content))
                .build();
*/


    }

    protected void signInSilently() {

        if(isNetworkConnected()) {
            mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                    new OnCompleteListener<GoogleSignInAccount>() {
                        @Override
                        public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                            if (task.isSuccessful()) {
                                onConnected(task.getResult());
                            } /*else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }*/
                        }
                    });
        }
        else{
            if(toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(MainActivity.this, "Please Connect to the Network", Toast.LENGTH_SHORT);
            toast.show();
            //Toast.makeText(MainActivity.this, "Please Connect to the Network", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        myAccount = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
        //Declering achievements and leadearboard and show
        ImageView leaderboard = (ImageView)findViewById(R.id.show_leaderboard_button);
        ImageView achievements = (ImageView)findViewById(R.id.show_achievements_button);


        //Declerating Mediplayer for button click audio
        final MediaPlayer[] player = {MediaPlayer.create(this, R.raw.button_click)};

        //Getting saved data
        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        int myIntValue = sp.getInt("points", 0);
        score = myIntValue;



        //Declering Press Button and setting listener on it
        pressButton = (Button)findViewById(R.id.pressButton);
        if(score != 0){
            pressButton.setText(Integer.toString(score));
        }
        pressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isNetworkConnected()) {
                    player[0].reset();
                    player[0] = MediaPlayer.create(MainActivity.this,R.raw.button_click);
                    player[0].start();
                    score++;
                    pressButton.setText(Integer.toString(score));
                    checkForAchievements(1, score);

                }
                else{
                    if(toast != null) {
                        toast.cancel();
                    }                    toast = Toast.makeText(MainActivity.this, "Please Connect to the Network", Toast.LENGTH_SHORT);
                    toast.show();

                    //Toast.makeText(MainActivity.this, "Please Connect to the Network", Toast.LENGTH_SHORT).show();
                }

            }
        });
        ///////////////////////////////////////////////////
        //Govazd Shanti yeterum
        final AdView adView = (AdView)findViewById(R.id.adView);
        //Change to the real banner
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
        //////////////////////////////////////////////
        //Govazddn avartvec, Shanti yeterum

        //Setting listener on Leaderboard and Achievements and Sign in Imagies
        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSignedIn()){
                    updateLeaderboard(score);
                    showLeaderboard();
                }
                else{
                    if(toast != null) {
                        toast.cancel();
                    }                    toast = Toast.makeText(MainActivity.this, "Please Sign In", Toast.LENGTH_SHORT);
                    //Toast.makeText(MainActivity.this, "Please Sign In", Toast.LENGTH_SHORT).show();
                    toast.show();
                }
            }
        });
        achievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSignedIn()) {
                    showAchievements();
                }
                else{
                    if(toast != null) {
                        toast.cancel();
                    }                    toast = Toast.makeText(MainActivity.this, "Please Sign In", Toast.LENGTH_SHORT);
                    //Toast.makeText(MainActivity.this, "Please Sign In", Toast.LENGTH_SHORT).show();
                    toast.show();

                }
            }
        });

        ImageView sign_in_button = (ImageView)findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
                myAccount = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

            }
        });
        ///////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        //signInSilently();
    }
    protected void handleException(Exception e, String details) {
        int status = 0;

        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            status = apiException.getStatusCode();
        }

        String message = getString(R.string.status_exception_error, details, status, e);

        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    protected void checkForAchievements(int increment, int score) {
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.
        if(!isSignedIn()){
            return;
        }
        if((score - increment) <= 5) {
            mAchievementsClient.increment(getString(R.string.achievement_5_click), increment);
        }
        if((score - increment) <= 206) {
            mAchievementsClient.increment(getString(R.string.achievement_206_click), increment);
        }
        if((score - increment) <= 407) {
            mAchievementsClient.increment(getString(R.string.achievement_407_click), increment);
        }
        if((score - increment) <= 608) {
            mAchievementsClient.increment(getString(R.string.achievement_608_click), increment);
        }
        if((score - increment) <= 809) {
            mAchievementsClient.increment(getString(R.string.achievement_809_click), increment);
        }
        if((score - increment) <= 910) {
            mAchievementsClient.increment(getString(R.string.achievement_910_click), increment);
        }
        if((score - increment) <= 1111) {
            mAchievementsClient.increment(getString(R.string.achievement_1111_click), increment);
        }
        if((score - increment) <= 1312) {
            mAchievementsClient.increment(getString(R.string.achievement_1312_click), increment);
        }
        if((score - increment) <= 1513) {
            mAchievementsClient.increment(getString(R.string.achievement_1513_click), increment);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }

                onDisconnected();

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        }
    }
    protected void onConnected(GoogleSignInAccount googleSignInAccount) {

        if(isNetworkConnected()) {
            if (isSignedIn()) {
                mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
                mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
                mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

                // Set the greeting appropriately on main menu
                mPlayersClient.getCurrentPlayer()
                        .addOnCompleteListener(new OnCompleteListener<Player>() {
                            @Override
                            public void onComplete(@NonNull Task<Player> task) {
                                if (!task.isSuccessful()) {
                                    Exception e = task.getException();
                                    handleException(e, getString(R.string.players_exception));
                                }
                            }
                        });
            }
            else{
                if(toast != null) {
                    toast.cancel();
                }                toast = Toast.makeText(MainActivity.this, "Please Sign In", Toast.LENGTH_SHORT);
                //Toast.makeText(MainActivity.this, "Please Sign In", Toast.LENGTH_SHORT).show();
                toast.show();
            }
        }
        else{
            if(toast != null) {
                toast.cancel();
            }            toast = Toast.makeText(MainActivity.this, "Please Connect to the Network", Toast.LENGTH_SHORT);
            //Toast.makeText(MainActivity.this, "Please Sign In", Toast.LENGTH_SHORT).show();
            toast.show();

        }
    }
    protected void onDisconnected() {
        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mPlayersClient = null;
        myAccount = null;


    }
    protected void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-1188194874657954/2618307211",
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
        mRewardedVideoAd.loadAd("ca-app-pub-1188194874657954/2618307211",
                new AdRequest.Builder().build());

    }
    @Override
    public void onRewarded(RewardItem rewardItem) {
        System.out.println("onRewarded");
        mRewardedVideoAd.loadAd("ca-app-pub-1188194874657954/2618307211",
                new AdRequest.Builder().build());
        score = score + 200;
        pressButton.setText(Integer.toString(score));
        checkForAchievements(200, score);
        updateLeaderboard(score);
        mRewardedVideoAd.loadAd("ca-app-pub-1188194874657954/2618307211",
                new AdRequest.Builder().build());

    }
    @Override
    public void onRewardedVideoAdLeftApplication() {
        System.out.println("onRewardedVideoAdLeftApplication");
        mRewardedVideoAd.loadAd("ca-app-pub-1188194874657954/2618307211",
                new AdRequest.Builder().build());


    }
    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        System.out.println("onRewardedVideoAdFailedToLoad");
        mRewardedVideoAd.loadAd("ca-app-pub-1188194874657954/2618307211",
                new AdRequest.Builder().build());



    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("points", score);
        editor.commit();
        updateLeaderboard(score);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateLeaderboard(score);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("points", score);
        editor.commit();
        updateLeaderboard(score);

    }
    // Sign in Method
    protected void signIn() {
        if(isNetworkConnected()) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        else{
            if(toast != null) {
                toast.cancel();
            }            toast = Toast.makeText(MainActivity.this, "Please Connect to the Network", Toast.LENGTH_SHORT);
            //Toast.makeText(MainActivity.this, "Please Sign In", Toast.LENGTH_SHORT).show();
            toast.show();
        }
    }


    //Check Network connection
    protected boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    //Is user Signed in?
    protected boolean isSignedIn() {

        return GoogleSignIn.getLastSignedInAccount(MainActivity.this) != null;
    }
    //Show Leaderboard method
    protected void showLeaderboard() {
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getLeaderboardIntent(getString(R.string.leaderboard_score))
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_LEADERBOARD_UI);
                    }
                });
    }


    protected void showAchievements() {
        Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_UNUSED);
                    }
                });
    }


    //Update Leaderboard method
    protected void updateLeaderboard(int score) {
        if (GoogleSignIn.getLastSignedInAccount(this) != null){
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .submitScore(getString(R.string.leaderboard_score), score);
        }
    }



    //When Back button pressed show toast messages
    @Override
    public void onBackPressed() {
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(MainActivity.this, "Already leaving ?", Toast.LENGTH_SHORT);
        //Toast.makeText(MainActivity.this, "Please Sign In", Toast.LENGTH_SHORT).show();
        toast.show();
        toast.cancel();
        toast = Toast.makeText(MainActivity.this, "Stay 5 more minutes :)", Toast.LENGTH_SHORT);
        //Toast.makeText(MainActivity.this, "Please Sign In", Toast.LENGTH_SHORT).show();
        toast.show();
        //Toast.makeText(MainActivity.this,"Already leaving ?",Toast.LENGTH_SHORT).show();
        //Toast.makeText(MainActivity.this,"Stay 5 more minutes :)",Toast.LENGTH_SHORT).show();


    }





}