package com.photovault.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int PICK_PHOTOS = 101;
    private static final int PICK_VIDEOS = 102;
    private static final int DELETE_REQUEST = 103;

    private static final String PIN = "1234";

    private final int BG = Color.rgb(13, 17, 23);
    private final int SURFACE = Color.rgb(22, 27, 34);
    private final int SURFACE_ALT = Color.rgb(33, 38, 45);
    private final int PRIMARY = Color.rgb(31, 111, 235);
    private final int GREEN = Color.rgb(35, 134, 54);
    private final int DANGER = Color.rgb(218, 54, 51);
    private final int TEXT = Color.rgb(230, 237, 243);
    private final int MUTED = Color.rgb(139, 148, 158);

    private LinearLayout galleryContainer;

    private final ArrayList<VaultItem> vaultItems =
            new ArrayList<>();

    private final ArrayList<Uri> pendingDeleteUris =
            new ArrayList<>();

    private int currentTab = 0;

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

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(30), dp(30), dp(30), dp(30));
        root.setBackgroundColor(BG);

        TextView icon = new TextView(this);
        icon.setText("🔐");
        icon.setTextSize(58);
        icon.setGravity(Gravity.CENTER);

        root.addView(
                icon,
                new LinearLayout.LayoutParams(-1, dp(90))
        );

        TextView title = new TextView(this);
        title.setText("PhotoVault");
        title.setTextColor(TEXT);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        root.addView(
                title,
                new LinearLayout.LayoutParams(-1, dp(55))
        );

        TextView subtitle = new TextView(this);
        subtitle.setText("خزنة خاصة للصور والفيديوهات");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);

        root.addView(
                subtitle,
                new LinearLayout.LayoutParams(-1, dp(45))
        );

        EditText pin = new EditText(this);
        pin.setHint("أدخل PIN");
        pin.setHintTextColor(MUTED);
        pin.setTextColor(TEXT);
        pin.setTextSize(20);
        pin.setGravity(Gravity.CENTER);

        pin.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        root.addView(
                pin,
                new LinearLayout.LayoutParams(-1, dp(60))
        );

        Button unlock = new Button(this);
        unlock.setText("فتح الخزنة");
        unlock.setTextColor(Color.WHITE);
        unlock.setTextSize(16);
        unlock.setAllCaps(false);
        unlock.setBackground(
                roundedBackground(PRIMARY, 14)
        );

        LinearLayout.LayoutParams unlockParams =
                new LinearLayout.LayoutParams(-1, dp(55));

        unlockParams.topMargin = dp(15);

        root.addView(unlock, unlockParams);

        TextView demo = new TextView(this);
        demo.setText("PIN التجريبي: 1234");
        demo.setTextColor(MUTED);
        demo.setTextSize(12);
        demo.setGravity(Gravity.CENTER);

        root.addView(
                demo,
                new LinearLayout.LayoutParams(-1, dp(40))
        );

        unlock.setOnClickListener(v -> {

            String entered =
                    pin.getText().toString().trim();

            if (entered.equals(PIN)) {
                showVault();
            } else {
                Toast.makeText(
                        this,
                        "❌ رمز PIN غير صحيح",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        setContentView(root);
    }

    // ============================================================
    // VAULT
    // ============================================================

    private void showVault() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(
                dp(16),
                dp(8),
                dp(10),
                dp(8)
        );
        header.setBackgroundColor(SURFACE);

        TextView title = new TextView(this);
        title.setText("🔐 PhotoVault");
        title.setTextColor(TEXT);
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        header.addView(
                title,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        Button lock = new Button(this);
        lock.setText("🔒");
        lock.setTextSize(18);
        lock.setAllCaps(false);

        lock.setOnClickListener(v -> showPinScreen());

        header.addView(
                lock,
                new LinearLayout.LayoutParams(
                        dp(55),
                        dp(50)
                )
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(-1, -2)
        );

        // ========================================================
        // TABS
        // ========================================================

        LinearLayout tabs = new LinearLayout(this);
        tabs.setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(5)
        );

        Button photos = new Button(this);
        photos.setText("🖼️ الصور");
        photos.setAllCaps(false);

        Button videos = new Button(this);
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
                new LinearLayout.LayoutParams(-1, -2)
        );

        photos.setOnClickListener(v -> {
            currentTab = 0;
            refreshGallery();
        });

        videos.setOnClickListener(v -> {
            currentTab = 1;
            refreshGallery();
        });

        // ========================================================
        // GALLERY
        // ========================================================

        ScrollView scroll = new ScrollView(this);

        galleryContainer = new LinearLayout(this);
        galleryContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        galleryContainer.setPadding(
                dp(8),
                dp(5),
                dp(8),
                dp(100)
        );

        scroll.addView(galleryContainer);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        // ========================================================
        // ADD BUTTON
        // ========================================================

        Button add = new Button(this);
        add.setText("＋ إضافة صور وفيديوهات");
        add.setTextColor(Color.WHITE);
        add.setTextSize(16);
        add.setAllCaps(false);
        add.setBackground(
                roundedBackground(PRIMARY, 15)
        );

        add.setOnClickListener(v -> showAddMenu());

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

        root.addView(add, addParams);

        setContentView(root);

        refreshGallery();
    }

    // ============================================================
    // ADD MENU
    // ============================================================

    private void showAddMenu() {

        String[] options = {
                "🖼️ اختيار الصور",
                "🎥 اختيار الفيديوهات"
        };

        new AlertDialog.Builder(this)
                .setTitle("إضافة إلى الخزنة")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        openPhotoPicker();
                    } else {
                        openVideoPicker();
                    }
                })
                .show();
    }

    // ============================================================
    // PHOTO PICKER
    // ============================================================

    private void openPhotoPicker() {

        Intent intent = new Intent(
                Intent.ACTION_OPEN_DOCUMENT
        );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType("image/*");

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                true
        );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        startActivityForResult(
                intent,
                PICK_PHOTOS
        );
    }

    // ============================================================
    // VIDEO PICKER
    // ============================================================

    private void openVideoPicker() {

        Intent intent = new Intent(
                Intent.ACTION_OPEN_DOCUMENT
        );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType("video/*");

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                true
        );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        startActivityForResult(
                intent,
                PICK_VIDEOS
        );
    }

    // ============================================================
    // RESULT
    // ============================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (resultCode != RESULT_OK ||
                data == null) {
            return;
        }

        if (requestCode == PICK_PHOTOS ||
                requestCode == PICK_VIDEOS) {

            ArrayList<Uri> uris =
                    new ArrayList<>();

            if (data.getClipData() != null) {

                int count =
                        data.getClipData().getItemCount();

                for (int i = 0; i < count; i++) {

                    uris.add(
                            data.getClipData()
                                    .getItemAt(i)
                                    .getUri()
                    );
                }

            } else if (data.getData() != null) {

                uris.add(data.getData());
            }

            boolean video =
                    requestCode == PICK_VIDEOS;

            importFiles(uris, video);
        }

        if (requestCode == DELETE_REQUEST) {

            completePendingDeletes();
        }
    }

    // ============================================================
    // IMPORT
    // ============================================================

    private void importFiles(
            ArrayList<Uri> uris,
            boolean video
    ) {

        if (uris.isEmpty()) {
            return;
        }

        pendingDeleteUris.clear();

        int imported = 0;

        for (Uri uri : uris) {

            try {

                String fileName =
                        createVaultFileName(uri, video);

                File destination =
                        new File(
                                getFilesDir(),
                                "vault"
                        );

                if (!destination.exists()) {
                    destination.mkdirs();
                }

                File target =
                        new File(
                                destination,
                                fileName
                        );

                copyUriToFile(uri, target);

                vaultItems.add(
                        new VaultItem(
                                target,
                                video,
                                uri.toString()
                        )
                );

                saveVaultIndex();

                pendingDeleteUris.add(uri);

                imported++;

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "تعذر إخفاء ملف: " +
                                e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        }

        saveVaultIndex();

        refreshGallery();

        if (pendingDeleteUris.isEmpty()) {

            Toast.makeText(
                    this,
                    "تمت الإضافة إلى الخزنة",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Android 11+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            try {

                Intent deleteIntent =
                        MediaStore.createDeleteRequest(
                                getContentResolver(),
                                pendingDeleteUris
                        );

                startIntentSenderForResult(
                        deleteIntent.getIntentSender(),
                        DELETE_REQUEST,
                        null,
                        0,
                        0,
                        0
                );

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "تم نسخ الملفات إلى الخزنة، " +
                                "لكن تعذر طلب حذف النسخ الأصلية.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } else {

            deleteOldAndroidVersions();

        }
    }

    // ============================================================
    // DELETE OLD ANDROID
    // ============================================================

    private void deleteOldAndroidVersions() {

        int deleted = 0;

        for (Uri uri : pendingDeleteUris) {

            try {

                int result =
                        getContentResolver()
                                .delete(
                                        uri,
                                        null,
                                        null
                                );

                if (result > 0) {
                    deleted++;
                }

            } catch (Exception ignored) {
            }
        }

        pendingDeleteUris.clear();

        Toast.makeText(
                this,
                "تم إخفاء " + deleted + " ملف",
                Toast.LENGTH_LONG
        ).show();
    }

    // ============================================================
    // AFTER USER CONFIRMS DELETE
    // ============================================================

    private void completePendingDeletes() {

        Toast.makeText(
                this,
                "✅ تم إخفاء الملفات من معرض الهاتف",
                Toast.LENGTH_LONG
        ).show();

        pendingDeleteUris.clear();

        refreshGallery();
    }

    // ============================================================
    // COPY URI -> FILE
    // ============================================================

    private void copyUriToFile(
            Uri uri,
            File target
    ) throws Exception {

        ContentResolver resolver =
                getContentResolver();

        InputStream input =
                resolver.openInputStream(uri);

        if (input == null) {
            throw new Exception(
                    "تعذر فتح الملف"
            );
        }

        FileOutputStream output =
                new FileOutputStream(target);

        byte[] buffer =
                new byte[8192];

        int length;

        while ((length = input.read(buffer)) != -1) {

            output.write(
                    buffer,
                    0,
                    length
            );
        }

        output.flush();
        output.close();
        input.close();
    }

    // ============================================================
    // FILE NAME
    // ============================================================

    private String createVaultFileName(
            Uri uri,
            boolean video
    ) {

        String extension =
                video ? ".mp4" : ".jpg";

        String time =
                new SimpleDateFormat(
                        "yyyyMMdd_HHmmss_SSS",
                        Locale.US
                ).format(new Date());

        return "vault_" +
                time +
                "_" +
                System.nanoTime() +
                extension;
    }

    // ============================================================
    // REFRESH
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
                currentTab == 0
                        ? "🖼️ الصور: " +
                          filtered.size()
                        : "🎥 الفيديوهات: " +
                          filtered.size()
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
                            dp(220)
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
            addThumbnail(grid, item);
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

        card.setPadding(
                dp(3),
                dp(3),
                dp(3),
                dp(3)
        );

        android.graphics.drawable.GradientDrawable cardBg =
                new android.graphics.drawable.GradientDrawable();

        cardBg.setColor(SURFACE);
        cardBg.setCornerRadius(dp(10));

        card.setBackground(cardBg);

        ImageView image =
                new ImageView(this);

        image.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        if (item.video) {

            Bitmap thumbnail =
                    ThumbnailUtils.createVideoThumbnail(
                            item.file.getAbsolutePath(),
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
                    Uri.fromFile(item.file)
            );
        }

        FrameLayout frame =
                new FrameLayout(this);

        frame.addView(
                image,
                new FrameLayout.LayoutParams(
                        -1,
                        dp(105)
                )
        );

        if (item.video) {

            TextView play =
                    new TextView(this);

            play.setText("▶");
            play.setTextColor(Color.WHITE);
            play.setTextSize(28);
            play.setGravity(Gravity.CENTER);

            frame.addView(
                    play,
                    new FrameLayout.LayoutParams(
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
        name.setGravity(Gravity.CENTER);

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
                "إرجاع إلى الهاتف",
                "حذف نهائياً"
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

                            } else if (which == 1) {

                                restoreFile(item);

                            } else {

                                deleteVaultFile(item);
                            }
                        }
                )
                .show();
    }

    // ============================================================
    // OPEN FILE
    // ============================================================

    private void openFile(
            VaultItem item
    ) {

        try {

            Uri uri =
                    Uri.fromFile(item.file);

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW
                    );

            intent.setDataAndType(
                    uri,
                    item.video
                            ? "video/*"
                            : "image/*"
            );

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "لا يوجد تطبيق مناسب لفتح الملف",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // RESTORE TO PHONE
    // ============================================================

    private void restoreFile(
            VaultItem item
    ) {

        try {

            ContentValues values =
                    new ContentValues();

            String mime =
                    item.video
                            ? "video/mp4"
                            : "image/jpeg";

            String relativePath =
                    item.video
                            ? Environment.DIRECTORY_MOVIES +
                              "/PhotoVault"
                            : Environment.DIRECTORY_PICTURES +
                              "/PhotoVault";

            values.put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    "restored_" +
                            System.currentTimeMillis() +
                            (item.video ? ".mp4" : ".jpg")
            );

            values.put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    mime
            );

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q) {

                values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        relativePath
                );
            }

            Uri collection =
                    item.video
                            ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            Uri newUri =
                    getContentResolver()
                            .insert(
                                    collection,
                                    values
                            );

            if (newUri == null) {

                throw new Exception(
                        "تعذر إنشاء الملف"
                );
            }

            OutputStream output =
                    getContentResolver()
                            .openOutputStream(newUri);

            if (output == null) {

                throw new Exception(
                        "تعذر فتح الملف الجديد"
                );
            }

            FileInputStream input =
                    new FileInputStream(item.file);

            byte[] buffer =
                    new byte[8192];

            int length;

            while ((length =
                    input.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        length
                );
            }

            input.close();
            output.flush();
            output.close();

            // نحذف النسخة من الخزنة بعد نجاح الإرجاع
            if (item.file.exists()) {
                item.file.delete();
            }

            vaultItems.remove(item);
            saveVaultIndex();
            refreshGallery();

            Toast.makeText(
                    this,
                    "✅ تم إرجاع الملف إلى الهاتف",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "خطأ في الإرجاع: " +
                            e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // DELETE FROM VAULT
    // ============================================================

    private void deleteVaultFile(
            VaultItem item
    ) {

        new AlertDialog.Builder(this)
                .setTitle("حذف نهائي")
                .setMessage(
                        "سيتم حذف هذا الملف نهائياً من الخزنة.\n" +
                        "لن يعود إلى معرض الهاتف."
                )
                .setPositiveButton(
                        "حذف",
                        (dialog, which) -> {

                            if (item.file.exists()) {
                                item.file.delete();
                            }

                            vaultItems.remove(item);
                            saveVaultIndex();

                            refreshGallery();

                            Toast.makeText(
                                    this,
                                    "تم الحذف نهائياً",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .setNegativeButton(
                        "إلغاء",
                        null
                )
                .show();
    }

    // ============================================================
    // VAULT INDEX
    // ============================================================

    private void saveVaultIndex() {

        try {

            File index =
                    new File(
                            getFilesDir(),
                            "vault_index.txt"
                    );

            FileOutputStream out =
                    new FileOutputStream(index);

            for (VaultItem item : vaultItems) {

                String line =
                        item.file.getName() +
                        "|" +
                        item.video +
                        "|" +
                        item.originalUri +
                        "\n";

                out.write(
                        line.getBytes("UTF-8")
                );
            }

            out.close();

        } catch (Exception ignored) {
        }
    }

    // ============================================================
    // LOAD VAULT
    // ============================================================

    private void loadVault() {

        vaultItems.clear();

        File folder =
                new File(
                        getFilesDir(),
                        "vault"
                );

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File index =
                new File(
                        getFilesDir(),
                        "vault_index.txt"
                );

        if (!index.exists()) {
            return;
        }

        try {

            java.io.BufferedReader reader =
                    new java.io.BufferedReader(
                            new java.io.InputStreamReader(
                                    new FileInputStream(index),
                                    "UTF-8"
                            )
                    );

            String line;

            while ((line =
                    reader.readLine()) != null) {

                String[] parts =
                        line.split(
                                "\\|",
                                3
                        );

                if (parts.length >= 2) {

                    File file =
                            new File(
                                    folder,
                                    parts[0]
                            );

                    boolean video =
                            Boolean.parseBoolean(
                                    parts[1]
                            );

                    String originalUri =
                            parts.length >= 3
                                    ? parts[2]
                                    : "";

                    if (file.exists()) {

                        vaultItems.add(
                                new VaultItem(
                                        file,
                                        video,
                                        originalUri
                                )
                        );
                    }
                }
            }

            reader.close();

        } catch (Exception ignored) {
        }
    }

    // ============================================================
    // BACKGROUND
    // ============================================================

    private android.graphics.drawable.GradientDrawable
    roundedBackground(
            int color,
            int radius
    ) {

        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();

        bg.setColor(color);
        bg.setCornerRadius(dp(radius));

        return bg;
    }

    // ============================================================
    // VAULT ITEM
    // ============================================================

    private static class VaultItem {

        File file;
        boolean video;
        String originalUri;

        VaultItem(
                File file,
                boolean video,
                String originalUri
        ) {

            this.file = file;
            this.video = video;
            this.originalUri = originalUri;
        }
    }
}