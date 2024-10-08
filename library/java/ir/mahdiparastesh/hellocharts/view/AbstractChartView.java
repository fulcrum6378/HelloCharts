package ir.mahdiparastesh.hellocharts.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.ViewCompat;

import ir.mahdiparastesh.hellocharts.animation.ChartAnimationListener;
import ir.mahdiparastesh.hellocharts.animation.ChartDataAnimator;
import ir.mahdiparastesh.hellocharts.animation.ChartViewportAnimator;
import ir.mahdiparastesh.hellocharts.calculator.ChartCalculator;
import ir.mahdiparastesh.hellocharts.gesture.ChartTouchHandler;
import ir.mahdiparastesh.hellocharts.gesture.ContainerScrollType;
import ir.mahdiparastesh.hellocharts.gesture.ZoomType;
import ir.mahdiparastesh.hellocharts.listener.ViewportChangeListener;
import ir.mahdiparastesh.hellocharts.model.SelectedValue;
import ir.mahdiparastesh.hellocharts.model.Viewport;
import ir.mahdiparastesh.hellocharts.renderer.AxesRenderer;
import ir.mahdiparastesh.hellocharts.renderer.ChartRenderer;
import ir.mahdiparastesh.hellocharts.util.ChartUtils;

public abstract class AbstractChartView extends View implements Chart {
    protected ChartCalculator chartCalculator;
    protected AxesRenderer axesRenderer;
    protected ChartTouchHandler touchHandler;
    protected ChartRenderer chartRenderer;
    protected ChartDataAnimator dataAnimator;
    protected ChartViewportAnimator viewportAnimator;
    protected boolean isInteractive = true;
    protected boolean isContainerScrollEnabled = false;
    protected ContainerScrollType containerScrollType;

    public AbstractChartView(Context context) {
        this(context, null, 0);
    }

    public AbstractChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbstractChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        chartCalculator = new ChartCalculator();
        touchHandler = new ChartTouchHandler(context, this);
        axesRenderer = new AxesRenderer(context, this);

        this.viewportAnimator = new ChartViewportAnimator(this);
        this.dataAnimator = new ChartDataAnimator(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        chartCalculator.setContentRect(getWidth(), getHeight(), getPaddingLeft(), getPaddingTop(), getPaddingRight(),
                getPaddingBottom());
        chartRenderer.onChartSizeChanged();
        axesRenderer.onChartSizeChanged();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isEnabled()) {
            axesRenderer.drawInBackground(canvas);
            int clipRestoreCount = canvas.save();
            canvas.clipRect(chartCalculator.getContentRectMinusAllMargins());
            chartRenderer.draw(canvas);
            canvas.restoreToCount(clipRestoreCount);
            chartRenderer.drawUnClipped(canvas);
            axesRenderer.drawInForeground(canvas);
        } else canvas.drawColor(ChartUtils.DEFAULT_COLOR);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (isInteractive) {
            if (isContainerScrollEnabled
                    ? touchHandler.handleTouchEvent(event, getParent(), containerScrollType) :
                    touchHandler.handleTouchEvent(event))
                ViewCompat.postInvalidateOnAnimation(this);
            return true;
        } else return false;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (isInteractive)
            if (touchHandler.computeScroll())
                ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void startDataAnimation() {
        dataAnimator.startAnimation(Long.MIN_VALUE);
    }

    @Override
    public void startDataAnimation(long duration) {
        dataAnimator.startAnimation(duration);
    }

    @Override
    public void cancelDataAnimation() {
        dataAnimator.cancelAnimation();
    }

    @Override
    public void animationDataUpdate(float scale) {
        getChartData().update(scale);
        chartRenderer.onChartViewportChanged();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void animationDataFinished() {
        getChartData().finish();
        chartRenderer.onChartViewportChanged();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void setDataAnimationListener(ChartAnimationListener animationListener) {
        dataAnimator.setChartAnimationListener(animationListener);
    }

    @Override
    public void setViewportAnimationListener(ChartAnimationListener animationListener) {
        viewportAnimator.setChartAnimationListener(animationListener);
    }

    @Override
    public void setViewportChangeListener(ViewportChangeListener viewportChangeListener) {
        chartCalculator.setViewportChangeListener(viewportChangeListener);
    }

    @Override
    public ChartRenderer getChartRenderer() {
        return chartRenderer;
    }

    @Override
    public void setChartRenderer(ChartRenderer renderer) {
        chartRenderer = renderer;
        resetRendererAndTouchHandler();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public AxesRenderer getAxesRenderer() {
        return axesRenderer;
    }

    @Override
    public ChartCalculator getChartCalculator() {
        return chartCalculator;
    }

    @Override
    public ChartTouchHandler getTouchHandler() {
        return touchHandler;
    }

    @Override
    public boolean isInteractive() {
        return isInteractive;
    }

    @Override
    public void setInteractive(boolean isInteractive) {
        this.isInteractive = isInteractive;
    }

    @Override
    public boolean isZoomEnabled() {
        return touchHandler.isZoomEnabled();
    }

    @Override
    public void setZoomEnabled(boolean isZoomEnabled) {
        touchHandler.setZoomEnabled(isZoomEnabled);
    }

    @Override
    public boolean isScrollEnabled() {
        return touchHandler.isScrollEnabled();
    }

    @Override
    public void setScrollEnabled(boolean isScrollEnabled) {
        touchHandler.setScrollEnabled(isScrollEnabled);
    }

    @Override
    public void moveTo(float x, float y) {
        Viewport scrollViewport = computeScrollViewport(x, y);
        setCurrentViewport(scrollViewport);
    }

    @Override
    public void moveToWithAnimation(float x, float y) {
        Viewport scrollViewport = computeScrollViewport(x, y);
        setCurrentViewportWithAnimation(scrollViewport);
    }

    private Viewport computeScrollViewport(float x, float y) {
        Viewport maxViewport = getMaximumViewport();
        Viewport currentViewport = getCurrentViewport();
        Viewport scrollViewport = new Viewport(currentViewport);

        if (maxViewport.contains(x, y)) {
            final float width = currentViewport.width();
            final float height = currentViewport.height();

            final float halfWidth = width / 2;
            final float halfHeight = height / 2;

            float left = x - halfWidth;
            float top = y + halfHeight;

            left = Math.max(maxViewport.left, Math.min(left, maxViewport.right - width));
            top = Math.max(maxViewport.bottom + height, Math.min(top, maxViewport.top));

            scrollViewport.set(left, top, left + width, top - height);
        }

        return scrollViewport;
    }

    @Override
    public boolean isValueTouchEnabled() {
        return touchHandler.isValueTouchEnabled();
    }

    @Override
    public void setValueTouchEnabled(boolean isValueTouchEnabled) {
        touchHandler.setValueTouchEnabled(isValueTouchEnabled);
    }

    @Override
    public ZoomType getZoomType() {
        return touchHandler.getZoomType();
    }

    @Override
    public void setZoomType(ZoomType zoomType) {
        touchHandler.setZoomType(zoomType);
    }

    @Override
    public float getMaxZoom() {
        return chartCalculator.getMaxZoom();
    }

    @Override
    public void setMaxZoom(float maxZoom) {
        chartCalculator.setMaxZoom(maxZoom);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public float getZoomLevel() {
        Viewport maxViewport = getMaximumViewport();
        Viewport currentViewport = getCurrentViewport();

        return Math.max(maxViewport.width() / currentViewport.width(),
                maxViewport.height() / currentViewport.height());

    }

    @Override
    public void setZoomLevel(float x, float y, float zoomLevel) {
        Viewport zoomViewport = computeZoomViewport(x, y, zoomLevel);
        setCurrentViewport(zoomViewport);
    }

    @Override
    public void setZoomLevelWithAnimation(float x, float y, float zoomLevel) {
        Viewport zoomViewport = computeZoomViewport(x, y, zoomLevel);
        setCurrentViewportWithAnimation(zoomViewport);
    }

    private Viewport computeZoomViewport(float x, float y, float zoomLevel) {
        final Viewport maxViewport = getMaximumViewport();
        Viewport zoomViewport = new Viewport(getMaximumViewport());

        if (maxViewport.contains(x, y)) {

            if (zoomLevel < 1) {
                zoomLevel = 1;
            } else if (zoomLevel > getMaxZoom()) {
                zoomLevel = getMaxZoom();
            }

            final float newWidth = zoomViewport.width() / zoomLevel;
            final float newHeight = zoomViewport.height() / zoomLevel;

            final float halfWidth = newWidth / 2;
            final float halfHeight = newHeight / 2;

            float left = x - halfWidth;
            float right = x + halfWidth;
            float top = y + halfHeight;
            float bottom = y - halfHeight;

            if (left < maxViewport.left) {
                left = maxViewport.left;
                right = left + newWidth;
            } else if (right > maxViewport.right) {
                right = maxViewport.right;
                left = right - newWidth;
            }

            if (top > maxViewport.top) {
                top = maxViewport.top;
                bottom = top - newHeight;
            } else if (bottom < maxViewport.bottom) {
                bottom = maxViewport.bottom;
                top = bottom + newHeight;
            }

            ZoomType zoomType = getZoomType();
            if (ZoomType.HORIZONTAL_AND_VERTICAL == zoomType) {
                zoomViewport.set(left, top, right, bottom);
            } else if (ZoomType.HORIZONTAL == zoomType) {
                zoomViewport.left = left;
                zoomViewport.right = right;
            } else if (ZoomType.VERTICAL == zoomType) {
                zoomViewport.top = top;
                zoomViewport.bottom = bottom;
            }

        }
        return zoomViewport;
    }

    @Override
    public Viewport getMaximumViewport() {
        return chartRenderer.getMaximumViewport();
    }

    @Override
    public void setMaximumViewport(Viewport maxViewport) {
        chartRenderer.setMaximumViewport(maxViewport);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void setCurrentViewportWithAnimation(Viewport targetViewport) {
        if (null != targetViewport) {
            viewportAnimator.cancelAnimation();
            viewportAnimator.startAnimation(getCurrentViewport(), targetViewport);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void setCurrentViewportWithAnimation(Viewport targetViewport, long duration) {
        if (null != targetViewport) {
            viewportAnimator.cancelAnimation();
            viewportAnimator.startAnimation(getCurrentViewport(), targetViewport, duration);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public Viewport getCurrentViewport() {
        return getChartRenderer().getCurrentViewport();
    }

    @Override
    public void setCurrentViewport(Viewport targetViewport) {
        if (null != targetViewport) {
            chartRenderer.setCurrentViewport(targetViewport);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void resetViewports() {
        chartRenderer.setMaximumViewport(null);
        chartRenderer.setCurrentViewport(null);
    }

    @Override
    public boolean isViewportCalculationEnabled() {
        return chartRenderer.isViewportCalculationEnabled();
    }

    @Override
    public void setViewportCalculationEnabled(boolean isEnabled) {
        chartRenderer.setViewportCalculationEnabled(isEnabled);
    }

    @Override
    public boolean isValueSelectionEnabled() {
        return touchHandler.isValueSelectionEnabled();
    }

    @Override
    public void setValueSelectionEnabled(boolean isValueSelectionEnabled) {
        touchHandler.setValueSelectionEnabled(isValueSelectionEnabled);
    }

    @Override
    public void selectValue(SelectedValue selectedValue) {
        chartRenderer.selectValue(selectedValue);
        callTouchListener();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public SelectedValue getSelectedValue() {
        return chartRenderer.getSelectedValue();
    }

    @Override
    public boolean isContainerScrollEnabled() {
        return isContainerScrollEnabled;
    }

    @Override
    public void setContainerScrollEnabled(boolean isContainerScrollEnabled, ContainerScrollType containerScrollType) {
        this.isContainerScrollEnabled = isContainerScrollEnabled;
        this.containerScrollType = containerScrollType;
    }

    protected void onChartDataChange() {
        chartCalculator.resetContentRect();
        chartRenderer.onChartDataChanged();
        axesRenderer.onChartDataChanged();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    protected void resetRendererAndTouchHandler() {
        this.chartRenderer.resetRenderer();
        this.axesRenderer.resetRenderer();
        this.touchHandler.resetTouchHandler();
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (getZoomLevel() <= 1.0)
            return false;
        final Viewport currentViewport = getCurrentViewport();
        final Viewport maximumViewport = getMaximumViewport();
        if (direction < 0)
            return currentViewport.left > maximumViewport.left;
        else return currentViewport.right < maximumViewport.right;
    }

    @Override
    public void setLabelMargin(int labelMargin) {
        this.chartRenderer.setLabelMargin(labelMargin);
    }

    @Override
    public void setLabelOffset(int labelOffset) {
        this.chartRenderer.setLabelOffset(labelOffset);
    }
}
