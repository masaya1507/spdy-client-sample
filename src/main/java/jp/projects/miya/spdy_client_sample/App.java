package jp.projects.miya.spdy_client_sample;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.spdy.SPDYClient;
import org.eclipse.jetty.spdy.api.DataInfo;
import org.eclipse.jetty.spdy.api.SPDY;
import org.eclipse.jetty.spdy.api.Session;
import org.eclipse.jetty.spdy.api.Stream;
import org.eclipse.jetty.spdy.api.StreamFrameListener;
import org.eclipse.jetty.spdy.api.StringDataInfo;
import org.eclipse.jetty.spdy.api.SynInfo;

/**
 */
public class App {

    public static void main(String[] args) throws Exception {

        // this listener receives data from the server. It then prints out the data
        StreamFrameListener streamListener = new StreamFrameListener.Adapter() {

            @Override
            public void onData(Stream stream, DataInfo dataInfo) {
                // Data received from server
                String content = dataInfo.asString("UTF-8", true);
                System.out.println("SPDY content: " + content);
            }
        };

        // Create client
        SPDYClient.Factory clientFactory = new SPDYClient.Factory();
        clientFactory.start();
        SPDYClient client = clientFactory.newSPDYClient(SPDY.V3);

        Session session = client.connect(new InetSocketAddress("google.co.jp", 80), null).get(5, TimeUnit.SECONDS);

        // Start a new session, and configure the stream listener
        final Stream stream = session.syn(new SynInfo(false), streamListener).get(5, TimeUnit.SECONDS);

        //start a timer that sends a request to this stream every second
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable periodicTask = new Runnable() {
            private int i = 0;

            public void run() {
                // send a request, don't close the stream
                stream.data(new StringDataInfo("Data from the client " + i++, false));
            }
        };
        executor.scheduleAtFixedRate(periodicTask, 0, 1, TimeUnit.SECONDS);
    }
}
