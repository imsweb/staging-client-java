package com.imsweb.decisionengine.basic;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.decisionengine.Endpoint.EndpointType;

public class BasicDataProviderTest {

    @Test
    public void testParseEndpoint() throws Exception {
        BasicEndpoint endpoint;
        BasicDataProvider provider = new BasicDataProvider();

        endpoint = provider.parseEndpoint("VALUE:123");
        Assert.assertEquals(EndpointType.VALUE, endpoint.getType());
        Assert.assertEquals("123", endpoint.getValue());

        endpoint = provider.parseEndpoint("VALUE:");
        Assert.assertEquals(EndpointType.VALUE, endpoint.getType());
        Assert.assertEquals("", endpoint.getValue());

        endpoint = provider.parseEndpoint("VALUE");
        Assert.assertEquals(EndpointType.VALUE, endpoint.getType());
        Assert.assertNull(endpoint.getValue());

        endpoint = provider.parseEndpoint("VALUE:ABC:123:XYZ");
        Assert.assertEquals(EndpointType.VALUE, endpoint.getType());
        Assert.assertEquals("ABC:123:XYZ", endpoint.getValue());
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