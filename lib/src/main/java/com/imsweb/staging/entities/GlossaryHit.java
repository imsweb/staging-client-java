/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package com.imsweb.staging.entities;

public class GlossaryHit {

    private final String _term;
    private final Integer _begin;
    private final Integer _end;

    public GlossaryHit(String term, Integer begin, Integer end) {
        _term = term;
        _begin = begin;
        _end = end;
    }

    public String getTerm() {
        return _term;
    }

    public Integer getBegin() {
        return _begin;
    }

    public Integer getEnd() {
        return _end;
    }
}
