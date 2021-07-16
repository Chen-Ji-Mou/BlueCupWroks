package com.chenjimou.bluecupwroks.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chenjimou.bluecupwroks.Constants;
import com.chenjimou.bluecupwroks.R;

import com.chenjimou.bluecupwroks.databinding.ActivityPictureDetailsBinding;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.utils.DisplayUtils;
import com.chenjimou.bluecupwroks.utils.DownLoadUtil;
import com.chenjimou.bluecupwroks.utils.ShareUtil;

public class PictureDetailsActivity extends AppCompatActivity
{
    ActivityPictureDetailsBinding mBinding;
    PictureBean mPictureBean;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityPictureDetailsBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.toolbarPictureDetailsActivity.setTitle("图片详情");
        setSupportActionBar(mBinding.toolbarPictureDetailsActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null)
        {
            mPictureBean = intent.getParcelableExtra(Constants.PICTURE_BEAN);

            if (mPictureBean != null)
            {
                Glide.with(this)
                        .load(mPictureBean.getDownload_url())
                        .override(DisplayUtils.getScreenWidth(this),
                                (DisplayUtils.getScreenWidth(this) * mPictureBean.getWidth() / mPictureBean.getHeight()))
                        .into(mBinding.imageViewPictureDetailsActivity);

                mBinding.authorPictureDetailsActivity.setText("作者："+ mPictureBean.getAuthor());
                mBinding.widthPictureDetailsActivity.setText("宽度："+ mPictureBean.getWidth());
                mBinding.heightPictureDetailsActivity.setText("高度："+ mPictureBean.getHeight());
            }
        }

        mBinding.shareButtonPictureDetailsActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                /* 选择应用进行分享图片 */
                ShareUtil.shareImage(mPictureBean.getId(), mPictureBean.getDownload_url(),
                        null,
                        null,
                        PictureDetailsActivity.this);
            }
        });

        mBinding.downLoadButtonPictureDetailsActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(PictureDetailsActivity.this,"开始下载，请稍后！", Toast.LENGTH_SHORT).show();
                DownLoadUtil.asynchronousDownload(mPictureBean.getId(), mPictureBean.getDownload_url(),PictureDetailsActivity.this);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
