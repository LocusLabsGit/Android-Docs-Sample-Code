package com.example.locuslabs.sdkexamplelibrary;

import com.locuslabs.sdk.maps.view.MapView;

public class HideWidgetsRunnable extends Thread {

    private ExampleShowHideWidgets example;

    public void  setExample(ExampleShowHideWidgets example){
        this.example = example;
    }

    public void run() {
        try {
            Thread.sleep(3000);
            example.hideAllWidgets();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
