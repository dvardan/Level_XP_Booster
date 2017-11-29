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
import android.view.Gravity;
import android.view.Window;
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
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import android.support.annotation.NonNull;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.level.xp.booster.levelxpbooster.MainMenuFragment.Listener;

import java.nio.BufferUnderflowException;
import java.util.concurrent.TimeUnit;



import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener, Listener{
    private Button pressButton;
    private int score;
    private RewardedVideoAd mRewardedVideoAd;



    // Fragments
    private MainMenuFragment mMainMenuFragment;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient;

    // Client variables
    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private PlayersClient mPlayersClient;

    // request codes we use when invoking an external activity
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_LEADERBOARD_UI = 9004;


    // tag for debug logging
    private static final String TAG = "TanC";

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    private final AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();






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
        signIn();

        // Create the fragments used by the UI.
        mMainMenuFragment = new MainMenuFragment();



        // Set the listeners and callbacks of fragment events.
        mMainMenuFragment.setListener(this);

        //Set Fragment
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                mMainMenuFragment).commit();
    }



    private void updateLeaderboard(int score) {
        if (GoogleSignIn.getLastSignedInAccount(this) != null){
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .submitScore(getString(R.string.leaderboard_score), score);
        }
    }

    private void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    private void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        final MediaPlayer[] player = {MediaPlayer.create(this, R.raw.button_click)};

        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        int myIntValue = sp.getInt("points", 0);
        score = myIntValue;
        System.out.println("stegh ay");
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
                    checkForAchievements(score);
                    updateLeaderboard(score);

                }
                else{
                    Toast.makeText(MainActivity.this, "Please Connect to the Network", Toast.LENGTH_SHORT).show();
                }

            }
        });


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

        ImageView leaderboard = (ImageView)findViewById(R.id.show_leaderboard_button);
        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLeaderboard(score);
                showLeaderboard();
            }
        });







    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();
    }

    private void signOut() {
        Log.d(TAG, "signOut()");

        if (!isSignedIn()) {
            Log.w(TAG, "signOut() called, but was not signed in!");
            return;
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        boolean successful = task.isSuccessful();
                        Log.d(TAG, "signOut(): " + (successful ? "success" : "failed"));

                        onDisconnected();
                    }
                });
    }


    @Override
    public void onShowAchievementsRequested() {
        mAchievementsClient.getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, getString(R.string.achievements_exception));
                    }
                });
    }



    private void handleException(Exception e, String details) {
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

    private void checkForAchievements(int score) {
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.
        if (score >= 5) {
            mOutbox.ach1 = true;
            achievementToast(getString(R.string.achievement_5_click));
        }
        else
        {
            return;
        }
        if (score >= 50) {
            mOutbox.ach2 = true;
            achievementToast(getString(R.string.achievement_50_click));
        }
        else
        {
            return;
        }
        if (score >= 100) {
            mOutbox.ach3 = true;
            achievementToast(getString(R.string.achievement_100_click));
        }
        else
        {
            return;
        }
        if (score >= 110) {
            mOutbox.ach4 = true;
            achievementToast(getString(R.string.achievement_200_click));
        }
        else
        {
            return;
        }
        if (score >= 120) {
            mOutbox.ach5 = true;
            achievementToast(getString(R.string.achievement_500_click));
        }
        else
        {
            return;
        }
        if (score >= 130) {
            mOutbox.ach6 = true;
            achievementToast(getString(R.string.achievement_600));
        }
        else
        {
            return;
        }

    }


    private void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }



    private void pushAccomplishments() {
        if (!isSignedIn()) {
            // can't push to the cloud, try again later
            return;
        }
        if (mOutbox.ach1) {
            mAchievementsClient.unlock(getString(R.string.achievement_5_click));
            mOutbox.ach1 = false;
        }
        if (mOutbox.ach2) {
            mAchievementsClient.unlock(getString(R.string.achievement_50_click));
            mOutbox.ach2 = false;
        }
        if (mOutbox.ach3) {
            mAchievementsClient.unlock(getString(R.string.achievement_100_click));
            mOutbox.ach3 = false;
        }
        if (mOutbox.ach4) {
            mAchievementsClient.unlock(getString(R.string.achievement_200_click));
            mOutbox.ach4 = false;
        }
        if (mOutbox.ach5) {
            mAchievementsClient.unlock(getString(R.string.achievement_500_click));
            mOutbox.ach5 = false;
        }
        if (mOutbox.ach6) {
            mAchievementsClient.unlock(getString(R.string.achievement_600));
            mOutbox.ach6 = false;
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


    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");

        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

        // Show sign-out button on main menu
        mMainMenuFragment.setShowSignInButton(false);

        // Show "you are signed in" message on win screen, with no sign in button.

        // Set the greeting appropriately on main menu
        mPlayersClient.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        if (task.isSuccessful()) {
                        } else {
                            Exception e = task.getException();
                            handleException(e, getString(R.string.players_exception));
                        }
                    }
                });


        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
                    Toast.LENGTH_LONG).show();
        }
    }


    private void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mPlayersClient = null;

        // Show sign-in button on main menu
        mMainMenuFragment.setShowSignInButton(true);

        // Show sign-in button on win screen

    }




    @Override
    public void onSignInButtonClicked() {
        startSignInIntent();
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
        score = score + 200;
        pressButton.setText(Integer.toString(score));



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
        editor.putInt("points", score);
        editor.commit();

    }
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("points", score);
        editor.commit();

    }






    private class AccomplishmentsOutbox {
        boolean ach1 = false;
        boolean ach2 = false;
        boolean ach3 = false;
        boolean ach4 = false;
        boolean ach5 = false;
        boolean ach6 = false;


        boolean isEmpty() {
            return !ach1 && !ach2 && !ach3 && !ach4 && !ach5 && !ach6;
        }

    }
    // Sign in Method
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //Check Network connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    //Is user Signed in?
    private boolean isSignedIn() {

        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }
    //Show Leaderboard method
    private void showLeaderboard() {
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getLeaderboardIntent(getString(R.string.leaderboard_score))
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_LEADERBOARD_UI);
                    }
                });
    }

}


