package com.example.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Button mMoveButton;
    Button mBackButton;
    Button mPlayStopButton;

    Timer mTimer;
    Handler mHandler = new Handler();

    // 画像ファイルのカーソル
    Cursor cursor;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMoveButton = (Button) findViewById(R.id.move_button);
        mBackButton = (Button) findViewById(R.id.back_button);
        mPlayStopButton = (Button) findViewById(R.id.play_stop_button);

        // ボタンの表示名称を「停止」に変更
        mPlayStopButton.setText("再生");

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

        // スライドショー進める
        mMoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.isLast()) {
                    cursor.moveToFirst();
                } else {
                    cursor.moveToNext();
                }
                getImageView(cursor);
            }
        });

        // スライドショー戻る
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.isFirst()) {
                    cursor.moveToLast();
                } else {
                    cursor.moveToPrevious();
                }
                getImageView(cursor);
            }
        });

        // スライドショー再生/停止
        mPlayStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 再生開始の場合
                if (mTimer == null) {
                    // 「進む」「戻る」ボタンを非活性
                    mMoveButton.setEnabled(false);
                    mBackButton.setEnabled(false);
                    // ボタンの表示名称を「停止」に変更
                    mPlayStopButton.setText("停止");
                    // タイマーの作成
                    mTimer = new Timer();
                    // タイマーの始動
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (cursor.isLast()) {
                                        cursor.moveToFirst();
                                    } else {
                                        cursor.moveToNext();
                                    }
                                    getImageView(cursor);
                                }
                            });
                        }
                    }, 2000, 2000);    // 最初に始動させるまで 100ミリ秒、ループの間隔を 100ミリ秒 に設定
                } else {
                    mTimer.cancel();
                    mTimer = null;
                    // ボタンの表示名称を「再生」に変更
                    mPlayStopButton.setText("再生");
                    // 「進む」「戻る」ボタンを活性
                    mMoveButton.setEnabled(true);
                    mBackButton.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            getImageView(cursor);
        } else {
            // 画像が見つからない場合は、各ボタンを非活性にする
            mMoveButton.setEnabled(false);
            mBackButton.setEnabled(false);
            mPlayStopButton.setEnabled(false);

            // 画像カーソルを閉じる
            cursor.close();
        }
    }

    private void getImageView(Cursor cursor) {
        // indexからIDを取得し、そのIDから画像のURIを取得する
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }
}
