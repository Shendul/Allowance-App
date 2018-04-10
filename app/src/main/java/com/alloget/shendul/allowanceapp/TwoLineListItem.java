package com.alloget.shendul.allowanceapp;

/**
 * Created by Shendul on 4/10/2018.
 */

public class TwoLineListItem {

    private String leftLine;
    private String rightLine;

    // Constructor that is used to create an instance of the Movie object
    public TwoLineListItem(String leftLine, String rightLine) {
        this.leftLine = leftLine;
        this.rightLine = rightLine;
    }

    public String getLeftLine() {
        return leftLine;
    }

    public void setLeftLine(String leftLine) {
        this.leftLine = leftLine;
    }

    public String getRightLine() {
        return rightLine;
    }

    public void setRightLine(String rightLine) {
        this.rightLine = rightLine;
    }
}
