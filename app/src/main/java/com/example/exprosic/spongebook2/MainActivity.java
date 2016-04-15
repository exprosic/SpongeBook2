package com.example.exprosic.spongebook2;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.exprosic.spongebook2.booklist.BookListFragment;
import com.example.exprosic.spongebook2.friend.FriendsFragment;
import com.example.exprosic.spongebook2.surroundings.SurroundingsFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.the_tablayout) TabLayout mTabLayout;
    @Bind(R.id.the_viewpager) ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (MyTabEnum.fromInt(position)) {
                    case MY_BOOKS:
                        return BookListFragment.newInstanceByUserId(0); // 0 for testing
                    case MY_FRIENDS:
                        return FriendsFragment.newInstance();
                    case SURROUNDING_USERS:
                        return SurroundingsFragment.newInstance();
                    default:
                        Log.e(TAG, String.format("no fragment at position %d", position));
                        return null;
                }
            }

            @Override
            public int getCount() {
                return MyTabEnum.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getResources().getString(MyTabEnum.fromInt(position).getTitleStringId());
            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
    }
}

enum MyTabEnum {
    MY_BOOKS(R.string.my_books),
    MY_FRIENDS(R.string.my_friends),
    SURROUNDING_USERS(R.string.surrounding_users);

    private static final List<MyTabEnum> theValues = Collections.unmodifiableList(Arrays.asList(values()));

    public static List<MyTabEnum> getValues() {
        return theValues;
    }

    public static int size() {
        return theValues.size();
    }

    public static MyTabEnum fromInt(int x) {
        return theValues.get(x);
    }

    private int mTitleStringId;

    MyTabEnum(int titleStringId) {
        mTitleStringId = titleStringId;
    }

    public int getTitleStringId() {
        return mTitleStringId;
    }
}