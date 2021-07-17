package com.chenjimou.bluecupwroks.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import com.chenjimou.bluecupwroks.R;
import com.chenjimou.bluecupwroks.databinding.ActivityPrivateCollectionBinding;
import com.chenjimou.bluecupwroks.ui.fragment.PrivateCollectionFragment;

public class PrivateCollectionActivity extends AppCompatActivity
{
    ActivityPrivateCollectionBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityPrivateCollectionBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
    }

    void init()
    {
        mBinding.toolbarPrivateCollection.setTitle("我的收藏");
        setSupportActionBar(mBinding.toolbarPrivateCollection);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_private_collection, new PrivateCollectionFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(
            @NonNull
                    MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}