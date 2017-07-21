package org.amv.trafficsoft.datahub.xfcd.event;


import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;

public interface XfcdDeliveryEvent extends XfcdEvent {
    TrafficsoftDeliveryPackage getDeliveryPackage();
}