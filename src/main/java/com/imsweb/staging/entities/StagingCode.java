package com.imsweb.staging.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"table", "code", "is_valid"})
public class StagingCode {

    protected String _table;
    protected String _code;
    protected Boolean _isValid;

    /**
     * Default constructor
     */
    public StagingCode() {
        setIsValid(false);
    }

    /**
     * @param table Table identifier
     * @param code Code
     * @param isValid Boolean indicating whether code is value
     */
    public StagingCode(String table, String code, Boolean isValid) {
        setTable(table);
        setCode(code);
        setIsValid(isValid);
    }

    @JsonProperty("table")
    public String getTable() {
        return _table;
    }

    public void setTable(String table) {
        _table = table;
    }

    @JsonProperty("code")
    public String getCode() {
        return _code;
    }

    public void setCode(String code) {
        _code = code;
    }

    @JsonProperty("is_valid")
    public Boolean getIsValid() {
        return _isValid;
    }

    public void setIsValid(Boolean isValid) {
        _isValid = isValid;
    }
}
