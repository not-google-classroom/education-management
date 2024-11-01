package com.org.education_management.util;


import com.org.education_management.model.Column;
import com.org.education_management.model.ForeignKey;
import com.org.education_management.model.Table;

public class SQLGenerator {
    public String generateCreateTableSQL(Table table) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(table.getTableName()).append(" (");

        for (Column column : table.getColumns()) {
            sql.append(column.getName()).append(" ").append(column.getType());

            if (Boolean.TRUE.equals(column.getNotNull())) {
                sql.append(" NOT NULL");
            }
            if (Boolean.TRUE.equals(column.getUnique())) {
                sql.append(" UNIQUE");
            }
            if (Boolean.TRUE.equals(column.getPrimaryKey())) {
                sql.append(" PRIMARY KEY");
            }
            if (column.getDefaultValue() != null) {
                sql.append(" DEFAULT ").append(column.getDefaultValue());
            }
            if (Boolean.TRUE.equals(column.getAutoIncrement())) {
                sql.append(" GENERATED ALWAYS AS IDENTITY");
            }
            sql.append(", ");
        }

        // Process Foreign Keys
        for (Column column : table.getColumns()) {
            if (column.getForeignKey() != null) {
                ForeignKey fk = column.getForeignKey();
                sql.append("FOREIGN KEY (").append(column.getName()).append(") REFERENCES ")
                        .append(fk.getReferencedTable()).append("(").append(fk.getReferencedColumn()).append(")");

                if (fk.getOnDelete() != null) {
                    sql.append(" ON DELETE ").append(fk.getOnDelete());
                }
                sql.append(", ");
            }
        }

        // Remove the last comma and space
        sql.setLength(sql.length() - 2);
        sql.append(");");

        return sql.toString();
    }
}
