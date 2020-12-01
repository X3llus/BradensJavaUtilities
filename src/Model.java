import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Model {

    protected String doublePattern = "^[0-9]+(\\.[0-9]+)?$";
    protected String datePattern = "^(((18|19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01]))|a)|" +
            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (0[1-9]|[12][0-9]|3[01]" +
            ") 00:00:00 [A-Z]+ (18|19|20)\\d\\d)$";
    protected String intPattern = "^\\d+$";

    private String camelCase = "^[a-z0-9][A-z0-9]{0,}";

    protected String table;
    protected List<String> columns;
    protected HashMap<String, Column> entry = new HashMap<>();

    /**
     * queries the tables schema to check for table with same name
     *
     * @throws SQLException if query is incorrect
     */
    protected void check() throws SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * \n" +
                    "FROM information_schema.tables\n" +
                    "WHERE table_schema = 'comp1011' \n" +
                    "    AND table_name = '" + table + "' \n" +
                    "LIMIT 1;";

            preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new SQLException("Table does not exist");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null)
                preparedStatement.close();
            if (conn != null)
                conn.close();
        }
    }

    /**
     * gets the column object
     *
     * @param column column name
     * @return column object with same name
     */
    public Column getColumn(String column) {
        return entry.get(column);
    }

    /**
     * gets the value from the column object
     *
     * @param column column name
     * @return value of column
     */
    public Object getValue(String column) {
        return entry.get(column).getValue();
    }

    /**
     * sets a columns value
     *
     * @param column column name
     * @param value value to set to
     */
    public void setValue(String column, Object value) {
        entry.get(column).setValue(value);
    }

    /**
     * gets the models entry
     *
     * @return the entry
     */
    public HashMap<String, Column> getEntry() {
        return entry;
    }

    /**
     * sets the values of all columns and validates all values.
     *
     * @param entry entry hashmap
     */
    public void create(HashMap<String, Object> entry) {
        entry.forEach((key, value) -> {

            if (!key.matches(camelCase)) {
                key = StringUtils.camelFormat(key);
            }

            if (this.columns.contains(key)) {
                if (value != null) {
                    if (value.toString().matches(datePattern)) {
                        try {
                            value = new SimpleDateFormat("yyyy-MM-dd").parse(value.toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    this.entry.get(key).setValue(value);
                }
            } else {
                throw new NoSuchElementException(key + " does not exist on object " + this.getClass().getName());
            }
        });
    }

    /**
     * Inserts object into database if no id value
     * Updates object in database if given id value
     *
     * @throws SQLException if sql queries incorrect
     */
    public void save() throws SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        int id = -1;

        try {

            List<String> columns = this.columns.stream().filter(column -> this.entry.get(column).getValue() != null
                    && !column.equals("id")).collect(Collectors.toList());
            String sql;
            StringBuilder sqlBuilder;

            if (this.entry.get("id").getValue() == null) {
                sqlBuilder = new StringBuilder("INSERT INTO ");
                sqlBuilder.append(table).append(" (");

                columns.forEach(column -> {
                    if (columns.indexOf(column) < columns.size() - 1) {
                        sqlBuilder.append(column).append(", ");
                    } else {
                        sqlBuilder.append(column).append(") VALUES (");
                    }
                });

                columns.forEach(column -> {
                    if (columns.indexOf(column) < columns.size() - 1) {
                        sqlBuilder.append("?, ");
                    } else {
                        sqlBuilder.append("?);");
                    }
                });
            } else {
                id = (int) this.entry.get("id").getValue();
                sqlBuilder = new StringBuilder("UPDATE ");
                sqlBuilder.append(table).append("\nset ");

                columns.forEach(column -> {
                    if (columns.indexOf(column) < columns.size() - 1) {
                        sqlBuilder.append(column).append(" = ?, ");
                    } else {
                        sqlBuilder.append(column).append(" = ?");
                    }
                });
                sqlBuilder.append("\n where id = ?");
            }

            sql = sqlBuilder.toString();
            conn = DBConnection.getConnection();
            preparedStatement = conn.prepareStatement(sql);

            for (int i = 1; i <= columns.size(); i++) {
                Column column = this.entry.get(columns.get(i - 1));
                if (column.getPattern().equals(doublePattern)) {
                    preparedStatement.setDouble(i, (Double) column.getValue());
                } else if (column.getPattern().equals(datePattern)) {
                    java.sql.Date date = new java.sql.Date(((java.util.Date) column.getValue()).getTime());
                    preparedStatement.setDate(i, date);
                } else if (column.getPattern().equals(intPattern)) {
                    preparedStatement.setInt(i, (int) column.getValue());
                } else {
                    preparedStatement.setString(i, (String) column.getValue());
                }
            }

            if (id > 0) {
                preparedStatement.setInt(columns.size() + 1, id);
            }

            preparedStatement.executeUpdate();

            if (id > 0 && id != (int) this.entry.get("id").getValue()) {
                ResultSet rs = preparedStatement.getGeneratedKeys();
                while (rs.next()) {
                    id = rs.getInt(1);
                }
                this.entry.get("id").setValue(id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null)
                preparedStatement.close();
            if (conn != null)
                conn.close();
        }
    }

    /**
     * gets all from table where columns match values given in entries
     *
     * @param entry incomplete entry object
     * @return list of entries
     * @throws SQLException if queries break
     */
    public List<HashMap<String, Object>> getEntry(HashMap<String, Object> entry) throws SQLException {

        Connection conn = null;
        PreparedStatement preparedStatement = null;

        try {
            String sql;
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(table).append(" WHERE ");
            List<String> columns = new ArrayList<>(entry.keySet());

            int i = 0;
            int size = columns.size();
            for (String column : columns) {
                if (i < size - 1) {
                    sqlBuilder.append(column).append(" = ? AND ");
                } else {
                    sqlBuilder.append(column).append(" = ?;");
                }
                i++;
            }

            sql = sqlBuilder.toString();
            conn = DBConnection.getConnection();
            preparedStatement = conn.prepareStatement(sql);

            i = 1;
            for (String column : columns) {
                preparedStatement.setObject(i, entry.get(column));
                i++;
            }

            return getHashMaps(preparedStatement, columns);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null)
                preparedStatement.close();
            if (conn != null)
                conn.close();
        }
        return null;
    }

    /**
     * gets all entries
     *
     * @return list off all entries
     * @throws SQLException if queries break
     */
    public List<HashMap<String, Object>> getAll() throws SQLException {

        Connection conn = null;
        PreparedStatement preparedStatement = null;

        try {
            String sql = "SELECT * FROM " + table + " LIMIT 500;";
            List<String> columns = new ArrayList<>(entry.keySet());
            conn = DBConnection.getConnection();
            preparedStatement = conn.prepareStatement(sql);
            return getHashMaps(preparedStatement, columns);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null)
                preparedStatement.close();
            if (conn != null)
                conn.close();
        }
        return null;
    }

    /**
     * intellij generated function
     * queries database and generates entry list
     *
     * @param preparedStatement sql statement
     * @param columns column list
     * @return results
     * @throws SQLException if queries break
     */
    private List<HashMap<String, Object>> getHashMaps(PreparedStatement preparedStatement, List<String> columns) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<HashMap<String, Object>> results = new ArrayList<>();

        while (resultSet.next()) {
            HashMap<String, Object> result = new HashMap<>();
            for (String column : columns) {
                result.put(column, resultSet.getObject(column));
            }
            results.add(result);
        }

        return results;
    }
}