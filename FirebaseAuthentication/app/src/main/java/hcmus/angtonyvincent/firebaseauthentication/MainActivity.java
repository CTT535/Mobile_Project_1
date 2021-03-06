package hcmus.angtonyvincent.firebaseauthentication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import hcmus.angtonyvincent.firebaseauthentication.list_room.ListRoomActivity;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    String TAG = "MainActivity";
    protected static int mPlayMode = 0;
    public static int MODE_SINGLE_PLAYER = 1;
    public static int MODE_MULTI_PLAYER = 2;

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
        //mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Tap on the image to enter\nPress back to exit this game", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static int getPlayMode(){
        return mPlayMode;
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
            final int number = getArguments().getInt(ARG_SECTION_NUMBER);

            TextView text = (TextView) rootView.findViewById(R.id.fragment_text);
            ImageView image = (ImageView) rootView.findViewById(R.id.fragment_image);

            switch (number) {
                case 1:
                    // Single Player
                    text.setText("Start a new journey");
                    image.setBackgroundResource(R.drawable.single_player);
                    break;
                case 2:
                    // Multi Player
                    text.setText("Play with your friends");
                    image.setBackgroundResource(R.drawable.multi_player);
                    break;
                case 3:
                    // Shop
                    text.setText("Purchase a new item");
                    image.setBackgroundResource(R.drawable.shop);
                    break;
                case 4:
                    // Tutorial
                    text.setText("Watch the tutorial video");
                    image.setBackgroundResource(R.drawable.tutorial);
                    break;
            }

            image.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        switch (number) {
                            case 1:
                                // Single player mode
                                mPlayMode = MODE_SINGLE_PLAYER;

                                Intent patternIntent = new Intent(getActivity(), PatternActivity.class);
                                startActivity(patternIntent);
                                getActivity().finish();
                                break;
                            case 2:
                                // Multi player mode
                                mPlayMode = MODE_MULTI_PLAYER;

                                Intent listRoomIntent = new Intent(getActivity(), ListRoomActivity.class);
                                startActivity(listRoomIntent);
                                getActivity().finish();
                                break;

                            case 3:
                            case 4:
                                Toast.makeText(getContext(), "This section is now not available", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                    return true;
                }
            });

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
