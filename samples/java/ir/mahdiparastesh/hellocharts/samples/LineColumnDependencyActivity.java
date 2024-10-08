package ir.mahdiparastesh.hellocharts.samples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import ir.mahdiparastesh.hellocharts.gesture.ZoomType;
import ir.mahdiparastesh.hellocharts.listener.ColumnChartOnValueSelectListener;
import ir.mahdiparastesh.hellocharts.model.Axis;
import ir.mahdiparastesh.hellocharts.model.AxisValue;
import ir.mahdiparastesh.hellocharts.model.Column;
import ir.mahdiparastesh.hellocharts.model.ColumnChartData;
import ir.mahdiparastesh.hellocharts.model.Line;
import ir.mahdiparastesh.hellocharts.model.LineChartData;
import ir.mahdiparastesh.hellocharts.model.PointValue;
import ir.mahdiparastesh.hellocharts.model.SubColumnValue;
import ir.mahdiparastesh.hellocharts.model.Viewport;
import ir.mahdiparastesh.hellocharts.util.ChartUtils;
import ir.mahdiparastesh.hellocharts.view.ColumnChartView;
import ir.mahdiparastesh.hellocharts.view.LineChartView;

public class LineColumnDependencyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_column_dependency);
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
    }

    public static class PlaceholderFragment extends Fragment {
        public final static String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec",};

        public final static String[] days = new String[]{"Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun",};

        private LineChartView chartTop;
        private ColumnChartView chartBottom;

        private LineChartData lineData;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_line_column_dependency, container, false);

            chartTop = rootView.findViewById(R.id.chart_top);
            generateInitialLineData();

            chartBottom = rootView.findViewById(R.id.chart_bottom);
            generateColumnData();

            return rootView;
        }

        private void generateColumnData() {

            int numSubColumns = 1;
            int numColumns = months.length;

            List<AxisValue> axisValues = new ArrayList<>();
            List<Column> columns = new ArrayList<>();
            List<SubColumnValue> values;
            for (int i = 0; i < numColumns; ++i) {

                values = new ArrayList<>();
                for (int j = 0; j < numSubColumns; ++j) {
                    values.add(new SubColumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
                }

                axisValues.add(new AxisValue(i).setLabel(months[i]));

                columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
            }

            ColumnChartData columnData = new ColumnChartData(columns);

            columnData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            columnData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(2));

            chartBottom.setColumnChartData(columnData);

            // Set value touch listener that will trigger changes for chartTop.
            chartBottom.setOnValueTouchListener(new ValueTouchListener());

            // Set selection mode to keep selected month column highlighted.
            chartBottom.setValueSelectionEnabled(true);

            chartBottom.setZoomType(ZoomType.HORIZONTAL);

            /*chartBottom.setOnClickListener(v -> {
                SelectedValue sv = chartBottom.getSelectedValue();
                if (!sv.isSet()) {
                    generateInitialLineData();
                }
            });*/

        }

        /**
         * Generates initial data for line chart. At the beginning all Y values are equals 0. That will change when user
         * will select value on column chart.
         */
        private void generateInitialLineData() {
            int numValues = 7;

            List<AxisValue> axisValues = new ArrayList<>();
            List<PointValue> values = new ArrayList<>();
            for (int i = 0; i < numValues; ++i) {
                values.add(new PointValue(i, 0));
                axisValues.add(new AxisValue(i).setLabel(days[i]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_GREEN).setCubic(true);

            List<Line> lines = new ArrayList<>();
            lines.add(line);

            lineData = new LineChartData(lines);
            lineData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

            chartTop.setLineChartData(lineData);

            // For build-up animation you have to disable viewport recalculation.
            chartTop.setViewportCalculationEnabled(false);

            // And set initial max viewport and current viewport- remember to set viewports after data.
            Viewport v = new Viewport(0, 110, 6, 0);
            chartTop.setMaximumViewport(v);
            chartTop.setCurrentViewport(v);

            chartTop.setZoomType(ZoomType.HORIZONTAL);
        }

        private void generateLineData(int color, float range) {
            // Cancel last animation if not finished.
            chartTop.cancelDataAnimation();

            // Modify data targets
            Line line = lineData.getLines().get(0);// For this example there is always only one line.
            line.setColor(color);
            for (PointValue value : line.getValues()) {
                // Change target only for Y value.
                value.setTarget(value.getX(), (float) Math.random() * range);
            }

            // Start new data animation with 300ms duration;
            chartTop.startDataAnimation(300);
        }

        private class ValueTouchListener implements ColumnChartOnValueSelectListener {
            @Override
            public void onValueSelected(int columnIndex, int subColumnIndex, SubColumnValue value) {
                generateLineData(value.getColor(), 100);
            }

            @Override
            public void onValueDeselected() {
                generateLineData(ChartUtils.COLOR_GREEN, 0);
            }
        }
    }
}
