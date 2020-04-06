package app.camera.tdoc.camera_library.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class CanvasView extends View {

    private Preview preview;
    private int[] measure_spec = new int[2];

    CanvasView(Context context, Bundle savedInstanceState, Preview preview) {
        super(context);
        this.preview = preview;


        final Handler handler = new Handler();
        Runnable tick = new Runnable() {
            public void run() {
                invalidate();
                handler.postDelayed(this, 100);
            }
        };
        tick.run();
    }

    @Override
    public void onDraw(Canvas canvas) {
        preview.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        preview.getMeasureSpec(measure_spec, widthSpec, heightSpec);
        super.onMeasure(measure_spec[0], measure_spec[1]);
    }
}
