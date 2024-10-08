package ir.mahdiparastesh.hellocharts.formatter;

import ir.mahdiparastesh.hellocharts.model.SubColumnValue;

public class SimpleColumnChartValueFormatter implements ColumnChartValueFormatter {

    private final ValueFormatterHelper valueFormatterHelper = new ValueFormatterHelper();

    public SimpleColumnChartValueFormatter() {
        valueFormatterHelper.determineDecimalSeparator();
    }

    public SimpleColumnChartValueFormatter(int decimalDigitsNumber) {
        this();
        valueFormatterHelper.setDecimalDigitsNumber(decimalDigitsNumber);
    }

    @Override
    public int formatChartValue(char[] formattedValue, SubColumnValue value) {
        return valueFormatterHelper.formatFloatValueWithPrependedAndAppendedText(formattedValue, value.getValue(),
                value.getLabelAsChars());
    }

    public int getDecimalDigitsNumber() {
        return valueFormatterHelper.getDecimalDigitsNumber();
    }

    public SimpleColumnChartValueFormatter setDecimalDigitsNumber(int decimalDigitsNumber) {
        valueFormatterHelper.setDecimalDigitsNumber(decimalDigitsNumber);
        return this;
    }

    public char[] getAppendedText() {
        return valueFormatterHelper.getAppendedText();
    }

    public SimpleColumnChartValueFormatter setAppendedText(char[] appendedText) {
        valueFormatterHelper.setAppendedText(appendedText);
        return this;
    }

    public char[] getPrependedText() {
        return valueFormatterHelper.getPrependedText();
    }

    public SimpleColumnChartValueFormatter setPrependedText(char[] prependedText) {
        valueFormatterHelper.setPrependedText(prependedText);
        return this;
    }

    public char getDecimalSeparator() {
        return valueFormatterHelper.getDecimalSeparator();
    }

    public SimpleColumnChartValueFormatter setDecimalSeparator(char decimalSeparator) {
        valueFormatterHelper.setDecimalSeparator(decimalSeparator);
        return this;
    }
}
