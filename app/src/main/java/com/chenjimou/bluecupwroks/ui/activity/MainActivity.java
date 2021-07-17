package com.chenjimou.bluecupwroks.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.chenjimou.bluecupwroks.R;
import com.chenjimou.bluecupwroks.databinding.ActivityMainBinding;
import com.chenjimou.bluecupwroks.ui.fragment.MainFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
    }

    void init()
    {
        setSupportActionBar(mBinding.toolbarMain);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                mBinding.drawerLayout, mBinding.toolbarMain,
                R.drawable.navigation, 0)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerToggle.syncState();
        mBinding.drawerLayout.addDrawerListener(mDrawerToggle);

        mBinding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull
                    MenuItem item)
            {
                if (item.getItemId() == R.id.nav_private_collection)
                    startActivity(new Intent(MainActivity.this, PrivateCollectionActivity.class));
                return false;
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_main, new MainFragment()).commit();
    }
}
