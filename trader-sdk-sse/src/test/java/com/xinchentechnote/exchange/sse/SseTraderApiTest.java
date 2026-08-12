package com.xinchentechnote.exchange.sse;

import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.sse.impl.NettySseTraderApi;
import org.junit.Test;

import static org.junit.Assert.*;

public class SseTraderApiTest {

    @Test
    public void testCreateDefaultFactory() {
        SseTraderApi api = SseTraderApi.CreateSseTraderApi();
        try {
            assertNotNull(api);
            assertTrue(api instanceof NettySseTraderApi);
            assertEquals(ApiStatus.NEW, api.getApiStatus());
            assertEquals("1.0.0", api.getApiVersion());
        } finally {
            ((NettySseTraderApi) api).shutdown();
        }
    }

    @Test
    public void testCreateWithNullArgs() throws Exception {
        SseTraderApi api = SseTraderApi.create(NettySseTraderApi.class, null);
        try {
            assertNotNull(api);
            assertTrue(api instanceof NettySseTraderApi);
        } finally {
            ((NettySseTraderApi) api).shutdown();
        }
    }

    @Test
    public void testCreateWithEmptyArgs() throws Exception {
        SseTraderApi api = SseTraderApi.create(NettySseTraderApi.class, new Object[0]);
        try {
            assertNotNull(api);
            assertTrue(api instanceof NettySseTraderApi);
        } finally {
            ((NettySseTraderApi) api).shutdown();
        }
    }
}
