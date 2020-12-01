public class Column<T> {
    private T value;
    private final String pattern;

    /**
     * sets the regex pattern
     *
     * @param pattern regex pattern
     */
    public Column(String pattern) {
        this.pattern = pattern;
    }

    /**
     * return value
     *
     * @return column value
     */
    public T getValue() {
        return value;
    }

    /**
     * checks the value against the pattern and if matches sets
     *
     * @param value value to set the column to
     */
    public void setValue(T value) {
        if (String.valueOf(value).matches(pattern))
            this.value = value;
        else
            throw new IllegalArgumentException(value + " does not match the pattern " + pattern + " on class " +
                    this.getClass().getName());
    }

    /**
     * returns the regex pattern used by column to validate
     *
     * @return regex pattern
     */
    public String getPattern() {
        return pattern;
    }
}
