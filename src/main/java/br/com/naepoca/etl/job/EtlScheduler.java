package br.com.naepoca.etl.job;

import br.com.naepoca.etl.service.EtlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EtlScheduler {

    private static final Logger log = LoggerFactory.getLogger(EtlScheduler.class);

    private final EtlService etlService;

    public EtlScheduler(EtlService etlService) {
        this.etlService = etlService;
    }

    @Scheduled(cron = "${naepoca.etl.cron:0 30 3 * * *}")
    public void nightly() {
        log.info("Starting nightly ETL");
        etlService.runAll(null);
    }
}
