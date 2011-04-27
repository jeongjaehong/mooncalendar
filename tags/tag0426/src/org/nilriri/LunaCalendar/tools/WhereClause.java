package org.nilriri.LunaCalendar.tools;

public class WhereClause {

    private StringBuffer clause;

    public WhereClause() {
        clause = new StringBuffer("1=1");
    }

    public boolean equals(Object object) {
        throw new RuntimeException("Stub!");
    }

    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    public void put(String key, String value) {
        clause.append(" and ").append(key).append("=");
        clause.append("'").append(value).append("'");
    }

    public void put(String key, Byte value) {
        clause.append(" and ").append(key).append("=");
        clause.append(value);
    }

    public void put(String key, Short value) {
        clause.append(" and ").append(key).append("=");
        clause.append(value);
    }

    public void put(String key, Integer value) {
        clause.append(" and ").append(key).append("=");
        clause.append(value);
    }

    public void put(String key, Long value) {
        clause.append(" and ").append(key).append("=");
        clause.append(value);
    }

    public void put(String key, Float value) {
        clause.append(" and ").append(key).append("=");
        clause.append(value);
    }

    public void put(String key, Double value) {
        clause.append(" and ").append(key).append("=");
        clause.append(value);
    }

    public void put(String key, Boolean value) {
        clause.append(" and ").append(key).append(" is ");
        clause.append((value ? "true" : "false"));
    }

    public void putNull(String key) {
        clause.append(" and ").append(key).append(" is null ");
    }

    public void clear() {
        clause = new StringBuffer("1=1");
    }

    public String toString() {
        return clause.toString();
    }

}