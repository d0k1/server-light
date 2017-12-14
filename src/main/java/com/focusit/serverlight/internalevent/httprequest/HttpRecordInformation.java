package com.focusit.serverlight.internalevent.httprequest;

import java.util.HashMap;

public class HttpRecordInformation
{
    public byte[] params;
    public byte[] payload;
    public HashMap<String, String> additional = new HashMap<>();
}
