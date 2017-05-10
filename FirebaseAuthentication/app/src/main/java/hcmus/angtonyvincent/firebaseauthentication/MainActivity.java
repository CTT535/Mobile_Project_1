package hcmus.angtonyvincent.firebaseauthentication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Press Back to exit this game", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            TextView description = (TextView) rootView.findViewById(R.id.menu_description_text);
            Button action = (Button) rootView.findViewById(R.id.menu_action_button);

            int number = getArguments().getInt(ARG_SECTION_NUMBER);

            switch (number) {
                case 1:
                    rootView.setBackgroundResource(R.drawable.menu1);
                    description.setText("Play with computer");
                    action.setText("Play");
                    action.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent patternIntent = new Intent(getActivity(), PatternActivity.class);
                            startActivity(patternIntent);
                            getActivity().finish();
                        }
                    });
                    break;
                case 2:
                    rootView.setBackgroundResource(R.drawable.menu2);
                    description.setText("Play with other players");
                    action.setText("Connect");
                    action.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getContext(), "NOT AVAILABLE", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case 3:
                    rootView.setBackgroundResource(R.drawable.menu3);
                    description.setText("Purchase new item");
                    action.setText("Go");
                    action.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getContext(), "NOT AVAILABLE", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case 4:
                    rootView.setBackgroundResource(R.drawable.menu4);
                    description.setText("Watch the tutorial");
                    action.setText("Watch");
                    action.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getContext(), "NOT AVAILABLE", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }

            return rootView;
        }
    } // PlaceholderFragment

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SINGLE PLAYER";
                case 1:
                    return "MULTI PLAYER";
                case 2:
                    return "SHOP";
                case 3:
                    return "TUTORIAL";
            }
            return null;
        }
    } // SectionsPagerAdapter
}
