package com.xinchentechnote.exchange.szse;

import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.szse.impl.NettySzseTraderApi;
import org.junit.Test;

import static org.junit.Assert.*;

public class SzseTraderApiTest {

    @Test
    public void testCreateDefaultFactory() {
        SzseTraderApi api = SzseTraderApi.CreateSzseTraderApi();
        assertNotNull(api);
        assertTrue(api instanceof NettySzseTraderApi);
        assertEquals(ApiStatus.NEW, api.getApiStatus());
        assertEquals("1.0.0", api.getApiVersion());
    }
}
