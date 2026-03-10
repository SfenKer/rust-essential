package com.github.sfenker.essential;

import java.text.DecimalFormat;

public final class Constants {

    public static String projectVersion = "{{ version }}";
    public static String gitTag = "{{ gitTag }}";
    public static String gitHash = "{{ gitHash }}";
    public static String gitBranch = "{{ gitBranch }}";

    public static final DecimalFormat decimalFormat =
        new DecimalFormat("#,###");

}
