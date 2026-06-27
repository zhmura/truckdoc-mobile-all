package com.sanda.truckdoc.client.ui.message;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.ui.dgcam.TouchImageView;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.commons.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Unified, in-app viewer for all supported attachment types:
 * <ul>
 *     <li><b>Images / GIF</b> — rendered with Glide (animated), pinch-to-zoom via {@link TouchImageView}.</li>
 *     <li><b>PDF</b> — rendered page-by-page with the platform {@link PdfRenderer} into a swipeable
 *     {@link ViewPager2}, each page pinch-zoomable, with a page indicator.</li>
 *     <li><b>Text</b> — scrollable, selectable text.</li>
 * </ul>
 * Provides an options menu to open the file in an external app or share it.
 */
public class FileViewerActivity extends AppCompatActivity {

    private static final String EXTRA_PATH = "file_path";
    private static final String AUTHORITY = "com.sanda.truckdoc.client.provider";
    private static final int MAX_PAGE_BITMAP_WIDTH = 2048; // keep within GPU texture limits

    private enum Kind { IMAGE, PDF, TEXT, OTHER }

    private File file;
    private Kind kind = Kind.OTHER;

    private TouchImageView imageView;
    private ProgressBar progress;

    @Nullable
    private ParcelFileDescriptor pdfFd;
    @Nullable
    private PdfRenderer pdfRenderer;

    public static void start(Context context, String absolutePath) {
        Intent intent = new Intent(context, FileViewerActivity.class);
        intent.putExtra(EXTRA_PATH, absolutePath);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        Toolbar toolbar = findViewById(R.id.file_viewer_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        imageView = findViewById(R.id.file_viewer_image);
        progress = findViewById(R.id.file_viewer_progress);

        String path = getIntent() != null ? getIntent().getStringExtra(EXTRA_PATH) : null;
        file = TextUtils.isEmpty(path) ? null : new File(path);
        if (file == null || !file.exists()) {
            Toast.makeText(this, R.string.cannot_open_file, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(file.getName());
        kind = detectKind(file);

        switch (kind) {
            case IMAGE:
                showImage();
                break;
            case PDF:
                showPdf();
                break;
            case TEXT:
                showText();
                break;
            default:
                // Not previewable in-app; hand off to an external app.
                openExternally();
                finish();
                break;
        }
    }

    private static Kind detectKind(File file) {
        String ext = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT);
        if (FileHelper.isImageExtension(ext)) {
            return Kind.IMAGE;
        }
        if ("pdf".equals(ext)) {
            return Kind.PDF;
        }
        if ("txt".equals(ext) || "log".equals(ext) || "csv".equals(ext)) {
            return Kind.TEXT;
        }
        return Kind.OTHER;
    }

    // --- Image ---------------------------------------------------------------

    private void showImage() {
        imageView.setVisibility(View.VISIBLE);
        imageView.setMaxZoom(6f);
        progress.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView);
        // Re-fit once Glide has set the drawable (load is async, after the first layout pass).
        imageView.postDelayed(() -> {
            progress.setVisibility(View.GONE);
            imageView.resetZoom();
        }, 150);
    }

    // --- PDF -----------------------------------------------------------------

    private void showPdf() {
        try {
            pdfFd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(pdfFd);
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_open_file, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final int pageCount = pdfRenderer.getPageCount();
        ViewPager2 pager = findViewById(R.id.file_viewer_pager);
        pager.setVisibility(View.VISIBLE);
        pager.setAdapter(new PdfPageAdapter());

        final TextView indicator = findViewById(R.id.file_viewer_page_indicator);
        if (pageCount > 1) {
            indicator.setVisibility(View.VISIBLE);
            indicator.setText(getString(R.string.file_viewer_page, 1, pageCount));
            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    indicator.setText(getString(R.string.file_viewer_page, position + 1, pageCount));
                }
            });
        }
    }

    private int targetBitmapWidth() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return Math.min((int) (dm.widthPixels * 1.5f), MAX_PAGE_BITMAP_WIDTH);
    }

    /** Renders a single PDF page to a bitmap. PdfRenderer permits only one open page at a time. */
    private Bitmap renderPdfPage(int index) {
        PdfRenderer renderer = pdfRenderer;
        if (renderer == null) {
            return null;
        }
        synchronized (renderer) {
            PdfRenderer.Page page = renderer.openPage(index);
            try {
                int width = targetBitmapWidth();
                int height = (int) ((long) width * page.getHeight() / page.getWidth());
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                // White background so transparent PDFs are readable.
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                return bitmap;
            } finally {
                page.close();
            }
        }
    }

    private class PdfPageAdapter extends RecyclerView.Adapter<PdfPageAdapter.PageHolder> {

        @NonNull
        @Override
        public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TouchImageView view = new TouchImageView(parent.getContext());
            view.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            view.setMaxZoom(6f);
            return new PageHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PageHolder holder, int position) {
            Bitmap bitmap = renderPdfPage(position);
            if (bitmap != null) {
                holder.image.setImageBitmap(bitmap);
                holder.image.post(holder.image::resetZoom);
            }
        }

        @Override
        public int getItemCount() {
            return pdfRenderer == null ? 0 : pdfRenderer.getPageCount();
        }

        class PageHolder extends RecyclerView.ViewHolder {
            final TouchImageView image;

            PageHolder(@NonNull TouchImageView itemView) {
                super(itemView);
                this.image = itemView;
            }
        }
    }

    // --- Text ----------------------------------------------------------------

    private void showText() {
        ScrollView scrollView = findViewById(R.id.file_viewer_text_scroll);
        TextView textView = findViewById(R.id.file_viewer_text);
        scrollView.setVisibility(View.VISIBLE);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lines = 0;
            while ((line = reader.readLine()) != null && lines < 20000) {
                sb.append(line).append('\n');
                lines++;
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_open_file, Toast.LENGTH_SHORT).show();
        }
        textView.setText(sb.toString());
    }

    // --- Options menu --------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_viewer, menu);
        MenuItem reset = menu.findItem(R.id.action_reset_zoom);
        if (reset != null) {
            reset.setVisible(kind == Kind.IMAGE);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_reset_zoom) {
            if (imageView != null) {
                imageView.resetZoom();
            }
            return true;
        } else if (id == R.id.action_open_with) {
            openExternally();
            return true;
        } else if (id == R.id.action_share) {
            shareFile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Uri fileUri() {
        return FileProvider.getUriForFile(this, AUTHORITY, file);
    }

    private static String mimeType(File file) {
        String ext = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT);
        switch (ext) {
            case "pdf": return "application/pdf";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "txt":
            case "log":
            case "csv": return "text/plain";
            case "apk": return "application/vnd.android.package-archive";
            default: return "*/*";
        }
    }

    private void openExternally() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri(), mimeType(file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, getString(R.string.file_viewer_open_with)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_open_file, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType(file));
            intent.putExtra(Intent.EXTRA_STREAM, fileUri());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, getString(R.string.file_viewer_share)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_open_file, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (pdfRenderer != null) {
                pdfRenderer.close();
            }
            if (pdfFd != null) {
                pdfFd.close();
            }
        } catch (Exception ignored) {
        }
    }
}
