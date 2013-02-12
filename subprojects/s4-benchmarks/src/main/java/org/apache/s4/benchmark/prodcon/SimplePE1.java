package org.apache.s4.benchmark.prodcon;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePE1 extends ProcessingElement {

    private static Logger logger = LoggerFactory.getLogger(SimplePE1.class);

    int nbInjectors;

    public void setNbInjectors(int nbInjectors) {
        this.nbInjectors = nbInjectors;
    }

    public void onEvent(Event event) {

        Long value = event.get("value", long.class);
        logger.trace(String.valueOf(value));
    }

    @Override
    protected void onRemove() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onCreate() {
        // TODO Auto-generated method stub

    }

}