package com.aiplatform.inspection.adapter;

import org.springframework.stereotype.Component;

@Component
public class DjiDockAdapter extends UnavailableCameraSdkAdapter {
    @Override
    public String vendor() {
        return "DJI_DOCK";
    }
}
