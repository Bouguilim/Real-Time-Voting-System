package com.journaldev.jsf.beans;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSetToMapConverter {

    public static List<Map<String, Object>> convert(ResultSet resultSet) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                resultList.add(row);
            }
        } catch (Exception e) {
            System.out.println("Error converting ResultSet to List of Map: " + e.getMessage());
        }
        return resultList;
    }
}
