package ca.allyscan.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** How a scan job ends when the site can't be crawled. */
class ScanFailureTest {

    private static final String ROOT = "https://dequeuniversity.com/demo/mars/";

    private final ScanJobRepository jobs = mock(ScanJobRepository.class);
    private final SiteCrawler crawler = mock(SiteCrawler.class);
    private final ScanService service = new ScanService(
            jobs, mock(PageRepository.class), mock(ViolationRepository.class), crawler);

    @Test
    void emptyCrawlIsAnErrorNotAnEmptyScan() {
        when(crawler.discover(ROOT)).thenReturn(List.of());

        var e = assertThrows(IllegalStateException.class, () -> service.discoverPages(ROOT));

        assertEquals("No pages could be found to scan at " + ROOT + ".", e.getMessage());
    }

    @Test
    void workerRecordsUnreachableSiteAsFailureAndNeverMarksDone() {
        ScanService mockService = mock(ScanService.class);
        ScanJob job = new ScanJob(ROOT);
        var unreachable = new SiteUnreachableException(ROOT, "the server refused the connection", null);
        when(mockService.claimNextJob()).thenReturn(Optional.of(job));
        when(mockService.discoverPages(ROOT)).thenThrow(unreachable);

        new ScanWorker(jobs, mockService).pollOnce();

        verify(mockService).recordFailure(job.getId(), unreachable.getMessage());
        verify(mockService, never()).markDone(any());
        verify(mockService, never()).runAxeOn(anyString());
    }

    @Test
    void repeatedFailuresRetryThenEndFailedWithMessage() {
        ScanJob job = new ScanJob(ROOT);
        when(jobs.findById(any())).thenReturn(Optional.of(job));
        String message = "Could not reach " + ROOT + " because the server refused the connection.";

        service.recordFailure(UUID.randomUUID(), message);
        assertEquals(ScanStatus.QUEUED, job.getStatus());
        assertNull(job.getErrorMessage());

        service.recordFailure(UUID.randomUUID(), message);
        assertEquals(ScanStatus.QUEUED, job.getStatus());

        service.recordFailure(UUID.randomUUID(), message);
        assertEquals(ScanStatus.FAILED, job.getStatus());
        assertEquals(message, job.getErrorMessage());
    }
}
