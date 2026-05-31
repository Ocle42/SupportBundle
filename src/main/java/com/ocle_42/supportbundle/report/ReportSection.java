package com.ocle_42.supportbundle.report;

@FunctionalInterface
public interface ReportSection<T> {
    T collect() throws Exception;
}
