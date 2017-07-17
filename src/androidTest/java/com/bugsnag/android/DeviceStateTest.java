package com.bugsnag.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class DeviceStateTest extends BugsnagTestCase {

    @Test
    public void testSaneValues() throws JSONException, IOException {
        Configuration config = new Configuration("some-api-key");
        DeviceState deviceState = new DeviceState(InstrumentationRegistry.getContext());
        JSONObject deviceStateJson = streamableToJson(deviceState);

        assertTrue(deviceStateJson.getLong("freeMemory") > 0);
        assertNotNull(deviceStateJson.get("orientation"));
        assertTrue(deviceStateJson.getDouble("batteryLevel") > 0);
        assertTrue(deviceStateJson.getBoolean("charging"));
        assertEquals("allowed", deviceStateJson.getString("locationStatus"));
        assertNotNull(deviceStateJson.get("networkAccess"));
        assertNotNull(deviceStateJson.get("time"));
    }
}
