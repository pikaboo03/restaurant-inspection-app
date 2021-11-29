package com.example.iter1_cmpt276.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * This class manages handling of violations that belong to single report in the android activity
 * by static instance method.
 */
public class ViolationReportManager implements Iterable<ViolationReport > {
    private List<ViolationReport> violationReports = new ArrayList<>();

    private static ViolationReportManager instance;

    public static ViolationReportManager getInstance() {
        if (instance == null) {
            instance = new ViolationReportManager();
        }
        return instance;
    }

    public ViolationReportManager(){
        // ensures singleton
    }

    public ArrayList<ViolationReport> returnList(){
        return (ArrayList<ViolationReport>) violationReports;
    }

    public int size(){return violationReports.size();}

    public void add(ViolationReport report) {
        violationReports.add(report);
    }
    public void remove(ViolationReport report) {
        violationReports.remove(report);
    }
    public ViolationReport get(int i) {
        return violationReports.get(i);
    }

    @Override
    public Iterator<ViolationReport> iterator() {
        return violationReports.iterator();
    }

    public int getNumReports() {
        return violationReports.size();
    }
}
