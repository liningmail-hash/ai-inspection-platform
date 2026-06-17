package com.aiplatform.inspection.adapter;

import org.springframework.stereotype.Component;

@Component
public class DahuaCameraSdkAdapter extends UnavailableCameraSdkAdapter {
    @Override
    public String vendor() {
        return "DAHUA";
    }
}
