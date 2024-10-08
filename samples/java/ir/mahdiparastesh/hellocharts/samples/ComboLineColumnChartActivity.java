package ir.mahdiparastesh.hellocharts.samples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import ir.mahdiparastesh.hellocharts.listener.ComboLineColumnChartOnValueSelectListener;
import ir.mahdiparastesh.hellocharts.model.Axis;
import ir.mahdiparastesh.hellocharts.model.Column;
import ir.mahdiparastesh.hellocharts.model.ColumnChartData;
import ir.mahdiparastesh.hellocharts.model.ComboLineColumnChartData;
import ir.mahdiparastesh.hellocharts.model.Line;
import ir.mahdiparastesh.hellocharts.model.LineChartData;
import ir.mahdiparastesh.hellocharts.model.PointValue;
import ir.mahdiparastesh.hellocharts.model.SubColumnValue;
import ir.mahdiparastesh.hellocharts.util.ChartUtils;
import ir.mahdiparastesh.hellocharts.view.ComboLineColumnChartView;

public class ComboLineColumnChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combo_line_column_chart);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    public static class PlaceholderFragment extends Fragment {

        private ComboLineColumnChartView chart;
        private ComboLineColumnChartData data;

        private int numberOfLines = 1;
        private final int maxNumberOfLines = 4;
        private final int numberOfPoints = 12;

        float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

        private boolean hasAxes = true;
        private boolean hasAxesNames = true;
        private boolean hasPoints = true;
        private boolean hasLines = true;
        private boolean isCubic = false;
        private boolean hasLabels = false;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_combo_line_column_chart, container, false);

            chart = rootView.findViewById(R.id.chart);
            chart.setOnValueTouchListener(new ValueTouchListener());

            generateValues();
            generateData();

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.combo_line_column_chart, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_reset) {
                reset();
                generateData();
                return true;
            }
            if (id == R.id.action_add_line) {
                addLineToData();
                return true;
            }
            if (id == R.id.action_toggle_lines) {
                toggleLines();
                return true;
            }
            if (id == R.id.action_toggle_points) {
                togglePoints();
                return true;
            }
            if (id == R.id.action_toggle_cubic) {
                toggleCubic();
                return true;
            }
            if (id == R.id.action_toggle_labels) {
                toggleLabels();
                return true;
            }
            if (id == R.id.action_toggle_axes) {
                toggleAxes();
                return true;
            }
            if (id == R.id.action_toggle_axes_names) {
                toggleAxesNames();
                return true;
            }
            if (id == R.id.action_animate) {
                prepareDataAnimation();
                chart.startDataAnimation();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void generateValues() {
            for (int i = 0; i < maxNumberOfLines; ++i) {
                for (int j = 0; j < numberOfPoints; ++j) {
                    randomNumbersTab[i][j] = (float) Math.random() * 50f + 5;
                }
            }
        }

        private void reset() {
            numberOfLines = 1;

            hasAxes = true;
            hasAxesNames = true;
            hasLines = true;
            hasPoints = true;
            hasLabels = false;
            isCubic = false;

        }

        private void generateData() {
            // Chart looks the best when line data and column data have similar maximum viewports.
            data = new ComboLineColumnChartData(generateColumnData(), generateLineData());

            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);
                if (hasAxesNames) {
                    axisX.setName("Axis X");
                    axisY.setName("Axis Y");
                }
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }

            chart.setComboLineColumnChartData(data);
        }

        private LineChartData generateLineData() {
            List<Line> lines = new ArrayList<>();
            for (int i = 0; i < numberOfLines; ++i) {
                List<PointValue> values = new ArrayList<>();
                for (int j = 0; j < numberOfPoints; ++j)
                    values.add(new PointValue(j, randomNumbersTab[i][j]));

                Line line = new Line(values);
                line.setColor(ChartUtils.COLORS[i]);
                line.setCubic(isCubic);
                line.setHasLabels(hasLabels);
                line.setHasLines(hasLines);
                line.setHasPoints(hasPoints);
                lines.add(line);
            }
            return new LineChartData(lines);
        }

        private ColumnChartData generateColumnData() {
            int numSubColumns = 1;
            int numColumns = 12;
            // Column can have many subColumns, here by default I use 1 subColumn in each of 8 columns.
            List<Column> columns = new ArrayList<>();
            List<SubColumnValue> values;
            for (int i = 0; i < numColumns; ++i) {
                values = new ArrayList<>();
                for (int j = 0; j < numSubColumns; ++j) {
                    values.add(new SubColumnValue((float) Math.random() * 50 + 5, ChartUtils.COLOR_GREEN));
                }
                columns.add(new Column(values));
            }
            return new ColumnChartData(columns);
        }

        private void addLineToData() {
            if (data.getLineChartData().getLines().size() >= maxNumberOfLines) {
                Toast.makeText(getActivity(), "Samples app uses max 4 lines!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                ++numberOfLines;
            }

            generateData();
        }

        private void toggleLines() {
            hasLines = !hasLines;
            generateData();
        }

        private void togglePoints() {
            hasPoints = !hasPoints;
            generateData();
        }

        private void toggleCubic() {
            isCubic = !isCubic;
            generateData();
        }

        private void toggleLabels() {
            hasLabels = !hasLabels;
            generateData();
        }

        private void toggleAxes() {
            hasAxes = !hasAxes;
            generateData();
        }

        private void toggleAxesNames() {
            hasAxesNames = !hasAxesNames;
            generateData();
        }

        private void prepareDataAnimation() {

            // Line animations
            for (Line line : data.getLineChartData().getLines()) {
                for (PointValue value : line.getValues()) {
                    // Here I modify target only for Y values but it is OK to modify X targets as well.
                    value.setTarget(value.getX(), (float) Math.random() * 50 + 5);
                }
            }

            // Columns animations
            for (Column column : data.getColumnChartData().getColumns()) {
                for (SubColumnValue value : column.getValues()) {
                    value.setTarget((float) Math.random() * 50 + 5);
                }
            }
        }

        private class ValueTouchListener implements ComboLineColumnChartOnValueSelectListener {

            @Override
            public void onValueDeselected() {
                // TODO Auto-generated method stub
            }

            @Override
            public void onColumnValueSelected(int columnIndex, int subColumnIndex, SubColumnValue value) {
                Toast.makeText(getActivity(), "Selected column: " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPointValueSelected(int lineIndex, int pointIndex, PointValue value) {
                Toast.makeText(getActivity(), "Selected line point: " + value, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
