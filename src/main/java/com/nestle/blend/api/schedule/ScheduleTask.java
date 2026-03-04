package com.nestle.blend.api.schedule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleTask {
    private Logger log = LogManager.getLogger(this.getClass());

    @Scheduled(fixedRate = 1)
    public void runEvery1h() {

    }
}
