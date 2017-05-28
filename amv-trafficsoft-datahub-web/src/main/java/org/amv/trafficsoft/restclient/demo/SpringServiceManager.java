package org.amv.trafficsoft.restclient.demo;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ServiceManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class SpringServiceManager extends AbstractIdleService implements
        InitializingBean, DisposableBean {

    private final ServiceManager delegate;

    public SpringServiceManager(ServiceManager delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void startUp() throws Exception {
        delegate.startAsync();
    }

    @Override
    protected void shutDown() throws Exception {
        delegate.stopAsync();
    }

    @Override
    public void destroy() throws Exception {
        shutDown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        startUp();
    }
}
