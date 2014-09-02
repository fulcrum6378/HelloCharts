package lecho.lib.hellocharts.renderer;

import lecho.lib.hellocharts.Chart;
import lecho.lib.hellocharts.ChartComputator;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.util.Utils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.text.TextUtils;

/**
 * Default axes renderer. Draws X axis below chart and Y axis on the left.
 */
public class DefaultAxesRenderer implements AxesRenderer {
	private static final int DEFAULT_AXIS_MARGIN_DP = 4;
	// Axis positions and also some tables indexes.
	private static final int TOP = 0;
	private static final int LEFT = 1;
	private static final int RIGHT = 2;
	private static final int BOTTOM = 3;

	private Chart chart;
	private int axisMargin;

	// 4 text paints for every axis, not all have to be used, indexed with TOP, LEFT, RIGHT, BOTTOM.
	private Paint[] textPaintTab = new Paint[] { new Paint(), new Paint(), new Paint(), new Paint() };
	private Paint linePaint;

	private float[] axisHorizontalDrawBuffer = new float[] {};;
	private final AxisStops axisHorizontalStopsBuffer = new AxisStops();

	private float[] axisVerticalDrawBuffer = new float[] {};
	private final AxisStops axisVerticalStopsBuffer = new AxisStops();

	private int[] axisLabelMaxWidthTab = new int[4];
	private int[] axisTextHeightTab = new int[4];
	private FontMetricsInt[] fontMetricsTab = new FontMetricsInt[] { new FontMetricsInt(), new FontMetricsInt(),
			new FontMetricsInt(), new FontMetricsInt() };

	private float[] valuesBuff = new float[1];
	private char[] labelBuffer = new char[32];
	private static final char[] labelWidthChars = new char[] { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
			'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0' };

	private float density;
	private float scaledDensity;

	public DefaultAxesRenderer(Context context, Chart chart) {
		this.chart = chart;

		density = context.getResources().getDisplayMetrics().density;
		scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		axisMargin = Utils.dp2px(density, DEFAULT_AXIS_MARGIN_DP);

		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(1);

		for (Paint paint : textPaintTab) {
			paint.setAntiAlias(true);
		}
	}

	@Override
	public void initAxesMeasurements() {

		int axisXTopHeight = initAxisMeasurement(chart.getChartData().getAxisXTop(), TOP);
		int axisXBottomHeight = initAxisMeasurement(chart.getChartData().getAxisXBottom(), BOTTOM);

		int axisYLeftWidth = initAxisMeasurement(chart.getChartData().getAxisYLeft(), LEFT);
		int axisYRightWidth = initAxisMeasurement(chart.getChartData().getAxisYRight(), RIGHT);

		chart.getChartComputator().setAxesMargin(axisYLeftWidth, axisXTopHeight, axisYRightWidth, axisXBottomHeight);
	}

	@Override
	public void draw(Canvas canvas) {
		if (null != chart.getChartData().getAxisYLeft()) {
			drawAxisVertical(canvas, LEFT);
		}

		if (null != chart.getChartData().getAxisYRight()) {
			drawAxisVertical(canvas, RIGHT);
		}

		if (null != chart.getChartData().getAxisXBottom()) {
			drawAxisHorizontal(canvas, BOTTOM);
		}

		if (null != chart.getChartData().getAxisXTop()) {
			drawAxisHorizontal(canvas, TOP);
		}
	}

	/**
	 * Initialize measurement for axes(left, right, top, bottom); Returns axis measured width( for left and right) or
	 * height(for top and bottom).
	 */
	private int initAxisMeasurement(Axis axis, int position) {
		if (null == axis) {
			return 0;
		}

		Typeface typeface = axis.getTypeface();
		if (null != typeface) {
			textPaintTab[position].setTypeface(typeface);
		}

		textPaintTab[position].setColor(axis.getTextColor());
		textPaintTab[position].setTextSize(Utils.sp2px(scaledDensity, axis.getTextSize()));
		textPaintTab[position].getFontMetricsInt(fontMetricsTab[position]);

		axisTextHeightTab[position] = Math.abs(fontMetricsTab[position].ascent);
		axisLabelMaxWidthTab[position] = (int) textPaintTab[position].measureText(labelWidthChars, 0,
				axis.getMaxLabelChars());

		int result = 0;

		if (LEFT == position || RIGHT == position) {

			int width = 0;

			// If auto-generated or has manual values add height for value labels.
			if (axis.isAutoGenerated() || !axis.getValues().isEmpty()) {
				width += axisLabelMaxWidthTab[position];
				width += axisMargin;
			}

			// If has name add height for axis name text.
			if (!TextUtils.isEmpty(axis.getName())) {
				width += axisTextHeightTab[position];
				width += axisMargin;
			}

			result = width;

		} else if (TOP == position || BOTTOM == position) {

			int height = 0;

			// If auto-generated or has manual values add height for value labels.
			if (axis.isAutoGenerated() || !axis.getValues().isEmpty()) {
				height += axisTextHeightTab[position];
			}

			// If has name add height for axis name text.
			if (!TextUtils.isEmpty(axis.getName())) {
				height += axisTextHeightTab[position];
				height += axisMargin;
			}

			result = height;

		} else {
			throw new IllegalArgumentException("Invalid axis position: " + position);
		}

		return result;
	}

	// ********** HORIZONTAL X AXES ****************

	private void drawAxisHorizontal(Canvas canvas, int position) {
		final ChartComputator computator = chart.getChartComputator();

		textPaintTab[position].setTextAlign(Align.CENTER);

		final Axis axis;
		final float rawY;
		final float nameBaseline;
		final float separationBaseline;

		if (BOTTOM == position) {
			axis = chart.getChartData().getAxisXBottom();
			rawY = computator.getContentRectWithMargins().bottom + axisTextHeightTab[position];
			nameBaseline = computator.getContentRectWithMargins().bottom + 2 * axisTextHeightTab[position] + axisMargin;
			separationBaseline = computator.getContentRect().bottom;
		} else if (TOP == position) {
			axis = chart.getChartData().getAxisXTop();
			rawY = computator.getContentRectWithMargins().top;
			nameBaseline = computator.getContentRectWithMargins().top - axisTextHeightTab[position] - axisMargin;
			separationBaseline = computator.getContentRect().top;
		} else {
			throw new IllegalArgumentException("Invalid position for horizontal axis: " + position);
		}

		if (axis.isAutoGenerated()) {
			drawAxisHorizontalAuto(canvas, axis, rawY, position);
		} else {
			drawAxisHorizontal(canvas, axis, rawY, position);
		}
		// Drawing axis name
		if (!TextUtils.isEmpty(axis.getName())) {
			canvas.drawText(axis.getName(), computator.getContentRect().centerX(), nameBaseline, textPaintTab[position]);
		}

		// Draw separation line with the same color as axis text. Only horizontal axes have separation lines.
		canvas.drawLine(computator.getContentRectWithMargins().left, separationBaseline,
				computator.getContentRectWithMargins().right, separationBaseline, textPaintTab[position]);
	}

	private void drawAxisHorizontal(Canvas canvas, Axis axis, float rawY, int position) {
		final ChartComputator computator = chart.getChartComputator();

		if (axis.hasLines() && axisHorizontalDrawBuffer.length < axis.getValues().size() * 4) {
			axisHorizontalDrawBuffer = new float[axis.getValues().size() * 4];
		}

		int i = 0;
		for (AxisValue axisValue : axis.getValues()) {
			final float value = axisValue.getValue();
			if (value >= computator.getVisibleViewport().left && value <= computator.getVisibleViewport().right) {
				final float rawX = computator.computeRawX(axisValue.getValue());
				valuesBuff[0] = axisValue.getValue();
				final int nummChars = axis.getFormatter().formatValue(labelBuffer, valuesBuff, axisValue.getLabel());
				canvas.drawText(labelBuffer, labelBuffer.length - nummChars, nummChars, rawX, rawY,
						textPaintTab[position]);

				if (axis.hasLines()) {
					axisHorizontalDrawBuffer[i * 4 + 0] = rawX;
					axisHorizontalDrawBuffer[i * 4 + 1] = computator.getContentRectWithMargins().top;
					axisHorizontalDrawBuffer[i * 4 + 2] = rawX;
					axisHorizontalDrawBuffer[i * 4 + 3] = computator.getContentRectWithMargins().bottom;
				}
			}
			++i;
		}

		if (axis.hasLines()) {
			linePaint.setColor(axis.getLineColor());
			canvas.drawLines(axisHorizontalDrawBuffer, 0, i, linePaint);
		}
	}

	private void drawAxisHorizontalAuto(Canvas canvas, Axis axis, float rawY, int position) {
		final ChartComputator computator = chart.getChartComputator();

		computeAxisStops(computator.getVisibleViewport().left, computator.getVisibleViewport().right, computator
				.getContentRect().width() / axisLabelMaxWidthTab[position] / 2, axisHorizontalStopsBuffer);

		if (axis.hasLines() && axisHorizontalDrawBuffer.length < axisHorizontalStopsBuffer.numStops * 4) {
			axisHorizontalDrawBuffer = new float[axisHorizontalStopsBuffer.numStops * 4];
		}

		for (int i = 0; i < axisHorizontalStopsBuffer.numStops; ++i) {
			float rawX = computator.computeRawX(axisHorizontalStopsBuffer.stops[i]);
			valuesBuff[0] = axisHorizontalStopsBuffer.stops[i];
			final int nummChars = axis.getFormatter().formatValue(labelBuffer, valuesBuff, null,
					axisHorizontalStopsBuffer.decimals);
			canvas.drawText(labelBuffer, labelBuffer.length - nummChars, nummChars, rawX, rawY, textPaintTab[position]);

			if (axis.hasLines()) {
				axisHorizontalDrawBuffer[i * 4 + 0] = rawX;
				axisHorizontalDrawBuffer[i * 4 + 1] = computator.getContentRectWithMargins().top;
				axisHorizontalDrawBuffer[i * 4 + 2] = rawX;
				axisHorizontalDrawBuffer[i * 4 + 3] = computator.getContentRectWithMargins().bottom;
			}
		}

		if (axis.hasLines()) {
			linePaint.setColor(axis.getLineColor());
			canvas.drawLines(axisHorizontalDrawBuffer, 0, axisHorizontalStopsBuffer.numStops * 4, linePaint);
		}
	}

	// ********** VERTICAL Y AXES ****************

	private void drawAxisVertical(Canvas canvas, int position) {
		final ChartComputator computator = chart.getChartComputator();

		final Axis axis;
		final float rawX;
		final float nameBaseline;

		if (LEFT == position) {
			axis = chart.getChartData().getAxisYLeft();
			textPaintTab[position].setTextAlign(Align.RIGHT);
			rawX = computator.getContentRectWithMargins().left - axisMargin;
			nameBaseline = computator.getContentRectWithMargins().left - axisLabelMaxWidthTab[position] - axisMargin
					* 2;
		} else if (RIGHT == position) {
			textPaintTab[position].setTextAlign(Align.LEFT);
			axis = chart.getChartData().getAxisYRight();
			rawX = computator.getContentRectWithMargins().right + axisMargin;
			nameBaseline = computator.getContentRectWithMargins().right + axisLabelMaxWidthTab[position] + axisMargin
					* 2 + axisTextHeightTab[position];
		} else {
			throw new IllegalArgumentException("Invalid position for horizontal axis: " + position);
		}

		// drawing axis values
		if (axis.isAutoGenerated()) {
			drawAxisVerticalAuto(canvas, axis, rawX, position);
		} else {
			drawAxisVertical(canvas, axis, rawX, position);
		}

		// drawing axis name
		if (!TextUtils.isEmpty(axis.getName())) {
			textPaintTab[position].setTextAlign(Align.CENTER);
			canvas.save();
			canvas.rotate(-90, computator.getContentRect().centerY(), computator.getContentRect().centerY());
			canvas.drawText(axis.getName(), computator.getContentRect().centerY(), nameBaseline, textPaintTab[position]);
			canvas.restore();
		}
	}

	private void drawAxisVertical(Canvas canvas, Axis axis, float rawX, int position) {
		final ChartComputator computator = chart.getChartComputator();

		if (axis.hasLines() && axisVerticalDrawBuffer.length < axis.getValues().size() * 4) {
			axisVerticalDrawBuffer = new float[axis.getValues().size() * 4];
		}

		int i = 0;
		for (AxisValue axisValue : axis.getValues()) {
			final float value = axisValue.getValue();
			if (value >= computator.getVisibleViewport().bottom && value <= computator.getVisibleViewport().top) {
				final float rawY = computator.computeRawY(value);
				valuesBuff[0] = axisValue.getValue();
				final int nummChars = axis.getFormatter().formatValue(labelBuffer, valuesBuff, axisValue.getLabel());
				canvas.drawText(labelBuffer, labelBuffer.length - nummChars, nummChars, rawX, rawY,
						textPaintTab[position]);

				if (axis.hasLines()) {
					axisVerticalDrawBuffer[i * 4 + 0] = computator.getContentRectWithMargins().left;
					axisVerticalDrawBuffer[i * 4 + 1] = rawY;
					axisVerticalDrawBuffer[i * 4 + 2] = computator.getContentRectWithMargins().right;
					axisVerticalDrawBuffer[i * 4 + 3] = rawY;
				}
			}

			++i;
		}

		if (axis.hasLines()) {
			linePaint.setColor(axis.getLineColor());
			canvas.drawLines(axisVerticalDrawBuffer, 0, i, linePaint);
		}
	}

	private void drawAxisVerticalAuto(Canvas canvas, Axis axis, float rawX, int position) {
		final ChartComputator computator = chart.getChartComputator();

		computeAxisStops(computator.getVisibleViewport().bottom, computator.getVisibleViewport().top, computator
				.getContentRect().height() / axisTextHeightTab[position] / 2, axisVerticalStopsBuffer);

		if (axis.hasLines() && axisVerticalDrawBuffer.length < axisVerticalStopsBuffer.numStops * 4) {
			axisVerticalDrawBuffer = new float[axisVerticalStopsBuffer.numStops * 4];
		}

		for (int i = 0; i < axisVerticalStopsBuffer.numStops; i++) {
			final float rawY = computator.computeRawY(axisVerticalStopsBuffer.stops[i]);
			valuesBuff[0] = axisVerticalStopsBuffer.stops[i];
			final int nummChars = axis.getFormatter().formatValue(labelBuffer, valuesBuff, null,
					axisVerticalStopsBuffer.decimals);
			canvas.drawText(labelBuffer, labelBuffer.length - nummChars, nummChars, rawX, rawY, textPaintTab[position]);

			if (axis.hasLines()) {
				axisVerticalDrawBuffer[i * 4 + 0] = computator.getContentRectWithMargins().left;
				axisVerticalDrawBuffer[i * 4 + 1] = rawY;
				axisVerticalDrawBuffer[i * 4 + 2] = computator.getContentRectWithMargins().right;
				axisVerticalDrawBuffer[i * 4 + 3] = rawY;
			}
		}

		if (axis.hasLines()) {
			linePaint.setColor(axis.getLineColor());
			canvas.drawLines(axisVerticalDrawBuffer, 0, axisVerticalStopsBuffer.numStops * 4, linePaint);
		}
	}

	/**
	 * Computes the set of axis labels to show given start and stop boundaries and an ideal number of stops between
	 * these boundaries.
	 * 
	 * @param start
	 *            The minimum extreme (e.g. the left edge) for the axis.
	 * @param stop
	 *            The maximum extreme (e.g. the right edge) for the axis.
	 * @param steps
	 *            The ideal number of stops to create. This should be based on available screen space; the more space
	 *            there is, the more stops should be shown.
	 * @param outStops
	 *            The destination {@link AxisStops} object to populate.
	 */
	private static void computeAxisStops(float start, float stop, int steps, AxisStops outStops) {
		double range = stop - start;
		if (steps == 0 || range <= 0) {
			outStops.stops = new float[] {};
			outStops.numStops = 0;
			return;
		}

		double rawInterval = range / steps;
		double interval = Utils.roundToOneSignificantFigure(rawInterval);
		double intervalMagnitude = Math.pow(10, (int) Math.log10(interval));
		int intervalSigDigit = (int) (interval / intervalMagnitude);
		if (intervalSigDigit > 5) {
			// Use one order of magnitude higher, to avoid intervals like 0.9 or 90
			interval = Math.floor(10 * intervalMagnitude);
		}

		double first = Math.ceil(start / interval) * interval;
		double last = Utils.nextUp(Math.floor(stop / interval) * interval);

		double intervalValue;
		int stopIndex;
		int numStops = 0;
		for (intervalValue = first; intervalValue <= last; intervalValue += interval) {
			++numStops;
		}

		outStops.numStops = numStops;

		if (outStops.stops.length < numStops) {
			// Ensure stops contains at least numStops elements.
			outStops.stops = new float[numStops];
		}

		for (intervalValue = first, stopIndex = 0; stopIndex < numStops; intervalValue += interval, ++stopIndex) {
			outStops.stops[stopIndex] = (float) intervalValue;
		}

		if (interval < 1) {
			outStops.decimals = (int) Math.ceil(-Math.log10(interval));
		} else {
			outStops.decimals = 0;
		}
	}

	/**
	 * A simple class representing axis label values used only for auto generated axes.
	 * 
	 */
	private static class AxisStops {
		float[] stops = new float[] {};
		int numStops;
		int decimals;
	}
}
