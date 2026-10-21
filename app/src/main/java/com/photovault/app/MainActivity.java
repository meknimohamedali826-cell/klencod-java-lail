package com.photovault.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int PICK_MEDIA = 100;
    private static final int DELETE_MEDIA = 200;

    // PIN التجريبي
    private static final String PIN = "1234";

    private final int BG = Color.rgb(13, 17, 23);
    private final int SURFACE = Color.rgb(22, 27, 34);
    private final int PRIMARY = Color.rgb(31, 111, 235);
    private final int DANGER = Color.rgb(218, 54, 51);
    private final int TEXT = Color.rgb(230, 237, 243);
    private final int MUTED = Color.rgb(139, 148, 158);

    private LinearLayout galleryContainer;

    private final ArrayList<VaultItem> vaultItems =
            new ArrayList<>();

    // الملفات الأصلية التي سيتم حذفها من المعرض
    private final ArrayList<Uri> pendingDeleteUris =
            new ArrayList<>();

    private int currentTab = 0;
    // 0 = الصور
    // 1 = الفيديوهات

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        showPinScreen();
    }

    // ============================================================
    // PIN
    // ============================================================

    private void showPinScreen() {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setGravity(Gravity.CENTER);

        layout.setPadding(
                dp(30),
                dp(30),
                dp(30),
                dp(30)
        );

        layout.setBackgroundColor(BG);

        TextView icon =
                new TextView(this);

        icon.setText("🔐");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);

        layout.addView(
                icon,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(90)
                )
        );

        TextView title =
                new TextView(this);

        title.setText("PhotoVault");
        title.setTextColor(TEXT);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        layout.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "خزنة خاصة للصور والفيديوهات"
        );

        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);

        layout.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                )
        );

        EditText pin =
                new EditText(this);

        pin.setHint("أدخل PIN");
        pin.setHintTextColor(MUTED);
        pin.setTextColor(TEXT);
        pin.setTextSize(20);
        pin.setGravity(Gravity.CENTER);

        pin.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        layout.addView(
                pin,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );

        Button unlock =
                new Button(this);

        unlock.setText("فتح الخزنة");
        unlock.setTextColor(Color.WHITE);
        unlock.setTextSize(16);
        unlock.setAllCaps(false);

        unlock.setBackground(
                roundedBackground(
                        PRIMARY,
                        14
                )
        );

        LinearLayout.LayoutParams unlockParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                );

        unlockParams.topMargin = dp(15);

        layout.addView(
                unlock,
                unlockParams
        );

        TextView demo =
                new TextView(this);

        demo.setText(
                "PIN التجريبي: 1234"
        );

        demo.setTextColor(MUTED);
        demo.setTextSize(12);
        demo.setGravity(Gravity.CENTER);

        layout.addView(
                demo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(40)
                )
        );

        unlock.setOnClickListener(v -> {

            String value =
                    pin.getText()
                            .toString()
                            .trim();

            if (value.equals(PIN)) {

                showVault();

            } else {

                Toast.makeText(
                        this,
                        "❌ رمز PIN غير صحيح",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        setContentView(layout);
    }

    // ============================================================
    // VAULT
    // ============================================================

    private void showVault() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        // ---------------- HEADER ----------------

        LinearLayout header =
                new LinearLayout(this);

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setPadding(
                dp(16),
                dp(10),
                dp(10),
                dp(10)
        );

        header.setBackgroundColor(SURFACE);

        TextView title =
                new TextView(this);

        title.setText("🔐 PhotoVault");
        title.setTextColor(TEXT);
        title.setTextSize(21);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        Button lock =
                new Button(this);

        lock.setText("🔒");
        lock.setTextSize(18);
        lock.setAllCaps(false);

        lock.setOnClickListener(
                v -> showPinScreen()
        );

        header.addView(
                lock,
                new LinearLayout.LayoutParams(
                        dp(55),
                        dp(50)
                )
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        // ---------------- TABS ----------------

        LinearLayout tabs =
                new LinearLayout(this);

        tabs.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(5)
        );

        Button photos =
                new Button(this);

        photos.setText("🖼️ الصور");
        photos.setAllCaps(false);

        Button videos =
                new Button(this);

        videos.setText("🎥 الفيديوهات");
        videos.setAllCaps(false);

        tabs.addView(
                photos,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        tabs.addView(
                videos,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        root.addView(
                tabs,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        photos.setOnClickListener(v -> {

            currentTab = 0;
            refreshGallery();
        });

        videos.setOnClickListener(v -> {

            currentTab = 1;
            refreshGallery();
        });

        // ---------------- SCROLL ----------------

        ScrollView scroll =
                new ScrollView(this);

        galleryContainer =
                new LinearLayout(this);

        galleryContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        galleryContainer.setPadding(
                dp(10),
                dp(5),
                dp(10),
                dp(100)
        );

        scroll.addView(
                galleryContainer
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        // ---------------- ADD ----------------

        Button add =
                new Button(this);

        add.setText(
                "＋ إضافة صور وفيديوهات"
        );

        add.setTextColor(Color.WHITE);
        add.setTextSize(16);
        add.setAllCaps(false);

        add.setBackground(
                roundedBackground(
                        PRIMARY,
                        15
                )
        );

        add.setOnClickListener(
                v -> openGallery()
        );

        LinearLayout.LayoutParams addParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        addParams.setMargins(
                dp(12),
                dp(5),
                dp(12),
                dp(10)
        );

        root.addView(
                add,
                addParams
        );

        setContentView(root);

        refreshGallery();
    }

    // ============================================================
    // GALLERY
    // ============================================================

    private void refreshGallery() {

        if (galleryContainer == null) {
            return;
        }

        loadVault();

        galleryContainer.removeAllViews();

        ArrayList<VaultItem> filtered =
                new ArrayList<>();

        for (VaultItem item : vaultItems) {

            if (currentTab == 0 &&
                    !item.video) {

                filtered.add(item);

            } else if (currentTab == 1 &&
                    item.video) {

                filtered.add(item);
            }
        }

        TextView count =
                new TextView(this);

        count.setText(
                (currentTab == 0
                        ? "🖼️ الصور: "
                        : "🎥 الفيديوهات: ")
                        + filtered.size()
        );

        count.setTextColor(TEXT);
        count.setTextSize(17);

        count.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        count.setPadding(
                dp(5),
                dp(8),
                dp(5),
                dp(12)
        );

        galleryContainer.addView(count);

        if (filtered.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    currentTab == 0
                            ? "لا توجد صور في الخزنة\n\nاضغط + لإضافة صور"
                            : "لا توجد فيديوهات في الخزنة\n\nاضغط + لإضافة فيديوهات"
            );

            empty.setTextColor(MUTED);
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);

            galleryContainer.addView(
                    empty,
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(200)
                    )
            );

            return;
        }

        GridLayout grid =
                new GridLayout(this);

        grid.setColumnCount(3);

        galleryContainer.addView(
                grid,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        for (VaultItem item : filtered) {

            addThumbnail(
                    grid,
                    item
            );
        }
    }

    // ============================================================
    // THUMBNAIL
    // ============================================================

    private void addThumbnail(
            GridLayout grid,
            VaultItem item
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER
        );

        ImageView image =
                new ImageView(this);

        image.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        File file = item.file;

        if (item.video) {

            android.graphics.Bitmap thumbnail =
                    ThumbnailUtils.createVideoThumbnail(
                            file.getAbsolutePath(),
                            MediaStore.Video.Thumbnails.MINI_KIND
                    );

            if (thumbnail != null) {

                image.setImageBitmap(
                        thumbnail
                );

            } else {

                image.setImageResource(
                        android.R.drawable.ic_media_play
                );
            }

        } else {

            image.setImageURI(
                    Uri.fromFile(file)
            );
        }

        android.widget.FrameLayout frame =
                new android.widget.FrameLayout(this);

        frame.addView(
                image,
                new android.widget.FrameLayout.LayoutParams(
                        -1,
                        dp(105)
                )
        );

        if (item.video) {

            TextView play =
                    new TextView(this);

            play.setText("▶");
            play.setTextColor(Color.WHITE);
            play.setTextSize(30);
            play.setGravity(
                    Gravity.CENTER
            );

            frame.addView(
                    play,
                    new android.widget.FrameLayout.LayoutParams(
                            -1,
                            dp(105)
                    )
            );
        }

        card.addView(
                frame,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(105)
                )
        );

        TextView name =
                new TextView(this);

        name.setText(
                item.file.getName()
        );

        name.setTextColor(MUTED);
        name.setTextSize(9);
        name.setGravity(
                Gravity.CENTER
        );

        card.addView(
                name,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        card.setOnClickListener(
                v -> showFileMenu(item)
        );

        GridLayout.LayoutParams params =
                new GridLayout.LayoutParams();

        params.width = 0;
        params.height = dp(140);

        params.columnSpec =
                GridLayout.spec(
                        GridLayout.UNDEFINED,
                        1f
                );

        params.setMargins(
                dp(3),
                dp(3),
                dp(3),
                dp(3)
        );

        grid.addView(
                card,
                params
        );
    }

    // ============================================================
    // FILE MENU
    // ============================================================

    private void showFileMenu(
            VaultItem item
    ) {

        String[] options = {
                "فتح",
                "حذف من الخزنة"
        };

        new AlertDialog.Builder(this)
                .setTitle(
                        item.video
                                ? "🎥 الفيديو"
                                : "🖼️ الصورة"
                )
                .setItems(
                        options,
                        (dialog, which) -> {

                            if (which == 0) {

                                openFile(item);

                            } else {

                                deleteVaultFile(item);
                            }
                        }
                )
                .show();
    }

    // ============================================================
    // OPEN
    // ============================================================

    private void openFile(
            VaultItem item
    ) {

        Intent intent =
                new Intent(
                        Intent.ACTION_VIEW
                );

        Uri uri =
                Uri.parse(
                        "content://com.aichat.app.vault/"
                                + Uri.encode(
                                item.file.getName()
                        )
                );

        /*
         * في هذه المرحلة نفتح الملف بواسطة
         * FileProvider.
         *
         * إذا أردنا تشغيل الفيديو والصور
         * داخل التطبيق نفسه، سنضيف شاشة Viewer
         * في الخطوة التالية.
         */

        Toast.makeText(
                this,
                "سيتم إضافة شاشة العرض الداخلية لاحقًا",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ============================================================
    // DELETE FROM VAULT
    // ============================================================

    private void deleteVaultFile(
            VaultItem item
    ) {

        new AlertDialog.Builder(this)
                .setTitle("حذف الملف")
                .setMessage(
                        "هل تريد حذف هذا الملف نهائيًا من الخزنة؟"
                )
                .setPositiveButton(
                        "حذف",
                        (dialog, which) -> {

                            if (item.file.delete()) {

                                Toast.makeText(
                                        this,
                                        "تم حذف الملف من الخزنة",
                                        Toast.LENGTH_SHORT
                                ).show();

                                refreshGallery();

                            } else {

                                Toast.makeText(
                                        this,
                                        "تعذر حذف الملف",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .setNegativeButton(
                        "إلغاء",
                        