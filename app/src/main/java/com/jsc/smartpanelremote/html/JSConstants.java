/*
 * Copyright (c) 2018. by RFControls. All Rights Reserved.
 * www.http://rfcontrols.com/
 * Design and Programming by Alex Dovby
 */

package com.jsc.smartpanelremote.html;

public class JSConstants {

    public static final String INTERFACE_NAME = "NativeApplication";
    // ---------------------------------------------
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    // ---------------------------------------------

    // events from client --------
    public static final int EVT_MAIN_TEST = 0;
    public static final int EVT_READY = 1;
    public static final int EVT_BACK = 4;
    // ---------------------------

    // events from ui app client --
    public static final int EVT_RUN_CMD = 5000;
    // ---------------------------

    // command for client --------
    public static final int CMD_INIT = 1000;
    // ---------------------------

    // events  app ---------------
    public static final int EVT_PAGE_FINISHED = 889;
    // ---------------------------
}
