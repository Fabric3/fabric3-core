package org.fabric3.discovery.etcd;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.api.MonitorChannel;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.discovery.ServiceEntry;
import org.fabric3.spi.runtime.event.EventService;
import org.junit.Assert;

public class EtcdDiscoveryAgentTest extends TestCase {
    private static final String RESPONSE_REGISTER = "{\"action\":\"set\",\"node\":{\"key\":\"/subdomain/services/runtime1:foo\","
                                                    + "\"value\":\"{\\\"key\\\":\\\"runtime1:foo\\\",\\\"name\\\":\\\"foo\\\",\\\"transport\\\":\\\"http\\\","
                                                    + "\\\"address\\\":\\\"localhost\\\",\\\"port\\\":2001,\\\"path\\\":\\\"foo\\\"}\",\"modifiedIndex\":120,"
                                                    + "\"createdIndex\":120},\"prevNode\":{\"key\":\"/services/runtime1:foo\","
                                                    + "\"value\":\"{\\\"key\\\":\\\"runtime1:foo\\\",\\\"name\\\":\\\"foo\\\",\\\"transport\\\":\\\"http\\\","
                                                    + "\\\"address\\\":\\\"localhost\\\",\\\"port\\\":2001,\\\"path\\\":\\\"foo\\\"}\",\"modifiedIndex\":119,"
                                                    + "\"createdIndex\":119}}";

    private static final String RESPONSE_GET = "{\"action\":\"get\",\"node\":{\"key\":\"/subdomain/services\",\"dir\":true,"
                                               + "\"nodes\":[{\"key\":\"/services/runtime1:foo\",\"value\":\"{\\\"key\\\":\\\"runtime1:foo\\\","
                                               + "\\\"name\\\":\\\"foo\\\",\\\"transport\\\":\\\"http\\\",\\\"address\\\":\\\"localhost\\\","
                                               + "\\\"port\\\":2001,\\\"path\\\":\\\"foo\\\"}\",\"modifiedIndex\":120,\"createdIndex\":120}],"
                                               + "\"modifiedIndex\":14,\"createdIndex\":14}}";

    private static final String RESPONSE_SET = "{\"action\":\"set\",\"node\":{\"key\":\"/subdomain/services/runtime1:foo\","
                                               + "\"value\":\"{\\\"key\\\":\\\"runtime1:foo\\\",\\\"name\\\":\\\"foo\\\",\\\"transport\\\":\\\"http\\\","
                                               + "\\\"address\\\":\\\"localhost\\\",\\\"port\\\":2001,\\\"path\\\":\\\"foo\\\"}\",\"modifiedIndex\":121,"
                                               + "\"createdIndex\":121},\"prevNode\":{\"key\":\"/services/runtime1:foo\","
                                               + "\"value\":\"{\\\"key\\\":\\\"runtime1:foo\\\",\\\"name\\\":\\\"foo\\\",\\\"transport\\\":\\\"http\\\","
                                               + "\\\"address\\\":\\\"localhost\\\",\\\"port\\\":2001,\\\"path\\\":\\\"foo\\\"}\",\"modifiedIndex\":120,"
                                               + "\"createdIndex\":120}}";

    private static final String RESPONSE_UNREGISTER = "{\"action\":\"delete\",\"node\":{\"key\":\"/subdomain/services/runtime1:foo\",\"modifiedIndex\":122,"
                                                      + "\"createdIndex\":121},\"prevNode\":{\"key\":\"/services/runtime1:foo\","
                                                      + "\"value\":\"{\\\"key\\\":\\\"runtime1:foo\\\",\\\"name\\\":\\\"foo\\\","
                                                      + "\\\"transport\\\":\\\"http\\\",\\\"address\\\":\\\"localhost\\\",\\\"port\\\":2001,"
                                                      + "\\\"path\\\":\\\"foo\\\"}\",\"modifiedIndex\":121," + "\"createdIndex\":121}}";

    private static final String RESPONSE_EXPIRE = "{\"action\":\"expire\",\"node\":{\"key\":\"/subdomain/services/runtime1:foo\",\"modifiedIndex\":130,"
                                                  + "\"createdIndex\":129},\"prevNode\":{\"key\":\"/services/runtime1:foo\","
                                                  + "\"value\":\"{\\\"key\\\":\\\"runtime1:foo\\\",\\\"name\\\":\\\"foo\\\",\\\"transport\\\":\\\"http\\\","
                                                  + "\\\"address\\\":\\\"localhost\\\",\\\"port\\\":2001,\\\"path\\\":\\\"foo\\\"}\","
                                                  + "\"expiration\":\"2015-04-21T09:54:22.085821125Z\",\"modifiedIndex\":129,\"createdIndex\":129}}";

    private static final int PORT = 4002;   // to run against etcd, change to 4001; some tests assume keys are already in etcd.

    private EtcdDiscoveryAgent agent;

    private MockWebServer server;

    public void testRegisterService() throws Exception {
        MockResponse response = new MockResponse();
        server.enqueue(response.setBody(RESPONSE_REGISTER));

        server.start(4002);

        agent.init();

        ServiceEntry entry = new ServiceEntry();
        entry.setName("foo");
        entry.setKey("runtime1:foo");
        entry.setAddress("localhost");
        entry.setPort(2001);
        entry.setPath("foo");
        entry.setTransport("http");
        agent.register(entry);

        if (PORT != 4002) {
            return; // skip if using etcd
        }
        RecordedRequest request = server.takeRequest();
        Assert.assertEquals("PUT", request.getMethod());
        Assert.assertEquals("/v2/keys/subdomain/services/runtime1:foo", request.getPath());
    }

    public void testGetServiceEntries() throws Exception {
        MockResponse response = new MockResponse();
        server.enqueue(response.setBody(RESPONSE_GET));

        server.start(4002);

        agent.init();

        List<ServiceEntry> entries = agent.getServiceEntries("foo");
        Assert.assertEquals(1, entries.size());
        Assert.assertEquals("foo", entries.get(0).getName());
    }

    public void testGetServiceChange() throws Exception {
        MockResponse response = new MockResponse();
        server.enqueue(response.setBody(RESPONSE_SET));
        server.start(4002);

        createExecutor(() -> null, () -> {
            Runnable runnable = (Runnable) EasyMock.getCurrentArguments()[0];
            runnable.run();
            return null;
        });

        agent.registerServiceListener("foo", (change, entry) -> {
            Assert.assertEquals(EntryChange.SET, change);
            Assert.assertEquals("foo", entry.getName());
            agent.destroy();
        });

        agent.init();
    }

    public void testServiceExpiration() throws Exception {
        MockResponse response = new MockResponse();
        server.enqueue(response.setBody(RESPONSE_EXPIRE));
        server.start(4002);

        agent.executorService = EasyMock.createMock(ExecutorService.class);
        agent.executorService.submit(EasyMock.isA(Runnable.class));

        createExecutor(() -> null, () -> {
            Runnable runnable = (Runnable) EasyMock.getCurrentArguments()[0];
            runnable.run();
            return null;
        });

        agent.registerServiceListener("foo", (change, entry) -> {
            Assert.assertEquals(EntryChange.EXPIRE, change);
            Assert.assertEquals("foo", entry.getName());
            agent.destroy();
        });

        agent.init();
    }

    public void testUnregisterService() throws Exception {
        MockResponse response = new MockResponse();
        server.enqueue(response.setBody(RESPONSE_UNREGISTER));

        server.start(4002);

        agent.init();
        agent.unregisterService("foo");

        if (PORT != 4002) {
            return; // skip if using etcd
        }
        RecordedRequest request = server.takeRequest();
        Assert.assertEquals("DELETE", request.getMethod());
        Assert.assertEquals("/v2/keys/subdomain/services/runtime1:foo", request.getPath());
    }

    @Override
    public void setUp() throws Exception {
        agent = new EtcdDiscoveryAgent();
        agent.setAddresses("http://127.0.0.1:" + PORT);

        agent.executorService = EasyMock.createNiceMock(ExecutorService.class);
        agent.eventService = EasyMock.createNiceMock(EventService.class);

        agent.info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(agent.info.getRuntimeName()).andReturn("runtime1").anyTimes();
        URI subdomain = URI.create("fabric3://subdomain");
        EasyMock.expect(agent.info.getDomain()).andReturn(subdomain).anyTimes();
        EasyMock.expect(agent.info.getZoneName()).andReturn("zone1").anyTimes();

        agent.monitor = EasyMock.createMock(MonitorChannel.class);
        agent.monitor.debug(EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(agent.info, agent.monitor, agent.executorService, agent.eventService);

        server = new MockWebServer();
    }

    @Override
    public void tearDown() throws Exception {
        server.shutdown();
    }

    private void createExecutor(IAnswer... answers) {
        agent.executorService = EasyMock.createMock(ExecutorService.class);
        agent.executorService.submit(EasyMock.isA(Runnable.class));
        for (IAnswer answer : answers) {
            EasyMock.expectLastCall().andStubAnswer(answer);
        }
        EasyMock.replay(agent.executorService);
    }

}