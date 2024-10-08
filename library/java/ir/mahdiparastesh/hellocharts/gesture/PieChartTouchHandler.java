package ir.mahdiparastesh.hellocharts.gesture;

import android.content.Context;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.OverScroller;

import ir.mahdiparastesh.hellocharts.view.PieChartView;

public class PieChartTouchHandler extends ChartTouchHandler {

    public static final int FLING_VELOCITY_DOWNSCALE = 4;
    protected OverScroller scroller;
    protected PieChartView pieChart;
    private boolean isRotationEnabled = true;

    public PieChartTouchHandler(Context context, PieChartView chart) {
        super(context, chart);
        pieChart = chart;
        scroller = new OverScroller(context);
        gestureDetector = new GestureDetector(context, new ChartGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ChartScaleGestureListener());
        isZoomEnabled = false;// Zoom is not supported by PieChart.
    }

    @Override
    public boolean computeScroll() {
        if (!isRotationEnabled) return false;
        if (scroller.computeScrollOffset()) {
            pieChart.setChartRotation(scroller.getCurrY(), false);
            // pieChart.setChartRotation() will invalidate view so no need to return true;
        }
        return false;
    }

    @Override
    public boolean handleTouchEvent(MotionEvent event) {
        boolean needInvalidate = super.handleTouchEvent(event);

        if (isRotationEnabled)
            needInvalidate = gestureDetector.onTouchEvent(event) || needInvalidate;
        return needInvalidate;
    }

    public boolean isRotationEnabled() {
        return isRotationEnabled;
    }

    public void setRotationEnabled(boolean isRotationEnabled) {
        this.isRotationEnabled = isRotationEnabled;
    }

    private static class ChartScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // No scale for PieChart.
            return false;
        }
    }

    private class ChartGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            if (isRotationEnabled) {
                scroller.abortAnimation();
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isRotationEnabled) {
                // Set the pie rotation directly.
                final RectF circleOval = pieChart.getCircleOval();
                final float centerX = circleOval.centerX();
                final float centerY = circleOval.centerY();
                float scrollTheta = vectorToScalarScroll(distanceX, distanceY, e2.getX() - centerX, e2.getY() -
                        centerY);
                pieChart.setChartRotation(pieChart.getChartRotation() - (int) scrollTheta / FLING_VELOCITY_DOWNSCALE,
                        false);
                return true;
            }

            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isRotationEnabled) {
                // Set up the Scroller for a fling
                final RectF circleOval = pieChart.getCircleOval();
                final float centerX = circleOval.centerX();
                final float centerY = circleOval.centerY();
                float scrollTheta = vectorToScalarScroll(velocityX, velocityY, e2.getX() - centerX, e2.getY() -
                        centerY);
                scroller.abortAnimation();
                scroller.fling(0, pieChart.getChartRotation(),
                        0, (int) scrollTheta / FLING_VELOCITY_DOWNSCALE,
                        0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                return true;
            }

            return false;
        }

        private float vectorToScalarScroll(float dx, float dy, float x, float y) {
            return (float) Math.sqrt(dx * dx + dy * dy) * Math.signum((-y * dx + x * dy));
        }
    }
}
