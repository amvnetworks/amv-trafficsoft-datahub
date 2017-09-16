package org.amv.trafficsoft.datahub.xfcd.event;


import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;

public interface TrafficsoftDeliveryEvent extends TrafficsoftEvent {
    TrafficsoftDeliveryPackage getDeliveryPackage();
}
