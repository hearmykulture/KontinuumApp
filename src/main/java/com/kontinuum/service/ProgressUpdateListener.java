package com.kontinuum.service;

import java.time.LocalDate;

public interface ProgressUpdateListener {
    void onProgressUpdated(LocalDate date);
}
