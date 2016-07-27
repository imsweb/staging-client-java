package com.imsweb.decisionengine.basic;

import org.junit.Test;

import com.imsweb.decisionengine.Endpoint.EndpointType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BasicDataProviderTest {

    @Test
    public void testParseEndpoint() throws Exception {
        BasicEndpoint endpoint;
        BasicDataProvider provider = new BasicDataProvider();

        endpoint = provider.parseEndpoint("VALUE:123");
        assertEquals(EndpointType.VALUE, endpoint.getType());
        assertEquals("123", endpoint.getValue());

        endpoint = provider.parseEndpoint("VALUE:");
        assertEquals(EndpointType.VALUE, endpoint.getType());
        assertEquals("", endpoint.getValue());

        endpoint = provider.parseEndpoint("VALUE");
        assertEquals(EndpointType.VALUE, endpoint.getType());
        assertNull(endpoint.getValue());

        endpoint = provider.parseEndpoint("VALUE:ABC:123:XYZ");
        assertEquals(EndpointType.VALUE, endpoint.getType());
        assertEquals("ABC:123:XYZ", endpoint.getValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testNullJumpValue() {
        BasicDataProvider provider = new BasicDataProvider();

        provider.parseEndpoint("JUMP");
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyJumpValue() {
        BasicDataProvider provider = new BasicDataProvider();

        provider.parseEndpoint("JUMP:");
    }

    @Test(expected = IllegalStateException.class)
    public void testBadType() {
        BasicDataProvider provider = new BasicDataProvider();

        provider.parseEndpoint("XXX:123");
    }
}