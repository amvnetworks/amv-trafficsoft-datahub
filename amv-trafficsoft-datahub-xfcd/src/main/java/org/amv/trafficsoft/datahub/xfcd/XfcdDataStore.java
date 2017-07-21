package org.amv.trafficsoft.datahub.xfcd;


public interface XfcdDataStore {
    void save(TrafficsoftDeliveryPackage deliveryPackage);

    boolean isPrimaryDataStore();
}
