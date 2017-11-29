package com.level.xp.booster.levelxpbooster;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
/**
 * Created by dvardan on 11/28/2017.
 */

public class MainMenuFragment extends Fragment implements OnClickListener {
    private View mShowAchievementsButton;

    interface Listener {

        // called when the user presses the `Show Achievements` button
        void onShowAchievementsRequested();

        // called when the user presses the `Sign In` button
        void onSignInButtonClicked();

    }

    private Listener mListener = null;
    private boolean mShowSignInButton = true;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mainmenu, container, false);

        final int[] clickableIds = new int[]{
                R.id.show_achievements_button,
                R.id.sign_in_button,
        };

        for (int clickableId : clickableIds) {
            view.findViewById(clickableId).setOnClickListener(this);
        }

        // cache views
        mShowAchievementsButton = view.findViewById(R.id.show_achievements_button);


        updateUI();

        return view;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }


    private void updateUI() {
        mShowAchievementsButton.setEnabled(!mShowSignInButton);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_achievements_button:
                mListener.onShowAchievementsRequested();
                break;
            case R.id.sign_in_button:
                mListener.onSignInButtonClicked();
                break;
        }
    }

    public void setShowSignInButton(boolean showSignInButton) {
        mShowSignInButton = showSignInButton;
        updateUI();
    }
}
