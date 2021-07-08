package com.chenjimou.bluecupwroks.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chenjimou.bluecupwroks.R;
import com.chenjimou.bluecupwroks.annotation.FindViewById;
import com.chenjimou.bluecupwroks.annotation.IntentExtra;
import com.chenjimou.bluecupwroks.utils.DownLoadUtil;
import com.chenjimou.bluecupwroks.utils.ImportUtil;
import com.chenjimou.bluecupwroks.utils.ShareUtil;

public class PictureDetailsActivity extends AppCompatActivity {

    @FindViewById(id = R.id.toolbar_PictureDetailsActivity)
    private Toolbar toolbar;

    @FindViewById(id = R.id.imageView_PictureDetailsActivity)
    private ImageView imageView;

    @FindViewById(id = R.id.author_PictureDetailsActivity)
    private TextView picture_author;

    @FindViewById(id = R.id.width_PictureDetailsActivity)
    private TextView picture_width;

    @FindViewById(id = R.id.height_PictureDetailsActivity)
    private TextView picture_height;

    @FindViewById(id = R.id.shareButton_PictureDetailsActivity)
    private Button shareButton;

    @FindViewById(id = R.id.downLoadButton_PictureDetailsActivity)
    private Button downLoadButton;

    @IntentExtra(key = "picture_id")
    private String picture_id;

    @IntentExtra(key = "author")
    private String author;

    @IntentExtra(key = "width")
    private int width;

    @IntentExtra(key = "height")
    private int height;

    @IntentExtra(key = "download_url")
    private String download_url;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_details);

        ImportUtil.importView(this);
        ImportUtil.importIntentExtras(this);

        toolbar.setTitle("图片详情");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null){
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            assert windowManager != null;
            int imageWidth = windowManager.getDefaultDisplay().getWidth();

            Glide.with(this)
                    .load(download_url)
                    .override(imageWidth, (imageWidth * width / height))
                    .into(imageView);

            picture_author.setText("作者："+ author);
            picture_width.setText("宽度："+ width);
            picture_height.setText("高度："+ height);
        }

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* 选择应用进行分享图片 */
                ShareUtil.shareImage(picture_id, download_url,
                        null,
                        null,
                        PictureDetailsActivity.this);
            }
        });

        downLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PictureDetailsActivity.this,"开始下载，请稍后！", Toast.LENGTH_SHORT).show();
                DownLoadUtil.asynchronousDownload(picture_id, download_url,PictureDetailsActivity.this);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
