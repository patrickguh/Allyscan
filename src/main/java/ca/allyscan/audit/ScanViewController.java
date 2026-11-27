package ca.allyscan.audit;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class ScanViewController {

    private final ScanService scanService;
    private final ScanJobRepository scanJobRepository;
    private final PageRepository pageRepository;
    private final ViolationRepository violationRepository;
    private final UserRepository userRepository;

    public ScanViewController(ScanService scanService,
                              ScanJobRepository scanJobRepository,
                              PageRepository pageRepository,
                              ViolationRepository violationRepository,
                              UserRepository userRepository) {
        this.scanService = scanService;
        this.scanJobRepository = scanJobRepository;
        this.pageRepository = pageRepository;
        this.violationRepository = violationRepository;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }

    /** One scanned page and its violations, with the small derived values the report window shows. */
    public record PageReport(Page page, List<Violation> violations) {

        private static final List<String> IMPACTS = List.of("critical", "serious", "moderate", "minor");

        /** The page's path ("/book"), shorter than the full URL for the page list. */
        public String path() {
            try {
                URI uri = new URI(page.getUrl());
                String path = uri.getRawPath();
                if (path == null || path.isEmpty()) path = "/";
                return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
            } catch (URISyntaxException e) {
                return page.getUrl();
            }
        }

        /** The page title when the page has one, else its path. */
        public String label() {
            String title = page.getTitle();
            return title == null || title.isBlank() ? path() : title;
        }

        public long count(String impact) {
            return violations.stream().filter(v -> impact.equalsIgnoreCase(v.getImpact())).count();
        }

        /** The most serious impact on the page ("critical" first), or null when it has none. */
        public String worstImpact() {
            return IMPACTS.stream().filter(i -> count(i) > 0).findFirst().orElse(null);
        }

        /** Violations ordered most serious first, so the top of each page's list is what to fix first. */
        public List<Violation> sortedViolations() {
            return violations.stream()
                    .sorted(Comparator.comparingInt(v -> {
                        int i = IMPACTS.indexOf(v.getImpact() == null ? "" : v.getImpact().toLowerCase());
                        return i < 0 ? IMPACTS.size() : i;
                    }))
                    .toList();
        }
    }

    public record CategorySummary(String slug, String label, long count) { }

    public record CriterionSummary(WcagCriterion criterion, long count) { }

    /** Everything the report window needs for one scan job. */
    public record Report(List<PageReport> pages, int totalViolations, List<CategorySummary> categories,
                         List<CriterionSummary> criteria, long bestPracticeCount) {

        /** Violations of one impact across every page. */
        public long count(String impact) {
            return pages.stream().mapToLong(pr -> pr.count(impact)).sum();
        }

        /** The most serious violation of the whole scan (the first page's first), or null when there is none. */
        public Violation worst() {
            return pages.stream()
                    .flatMap(pr -> pr.sortedViolations().stream().limit(1))
                    .min(Comparator.comparingInt(v -> {
                        int i = List.of("critical", "serious", "moderate", "minor")
                                .indexOf(v.getImpact() == null ? "" : v.getImpact().toLowerCase());
                        return i < 0 ? 4 : i;
                    }))
                    .orElse(null);
        }
    }

    private Report buildReport(UUID jobId) {
        List<PageReport> pages = pageRepository.findByScanJobId(jobId).stream()
                .map(page -> new PageReport(page, violationRepository.findByPageId(page.getId())))
                .sorted(Comparator.comparingInt((PageReport pr) -> pr.violations().size()).reversed())
                .toList();

        int total = pages.stream().mapToInt(pr -> pr.violations().size()).sum();

        List<CategorySummary> categories = pages.stream()
                .flatMap(pr -> pr.violations().stream())
                .collect(Collectors.groupingBy(Violation::getCategory, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new CategorySummary(e.getKey().getSlug(), e.getKey().getLabel(), e.getValue()))
                .sorted(Comparator.comparingLong(CategorySummary::count).reversed())
                .toList();

        List<CriterionSummary> criteria = pages.stream()
                .flatMap(pr -> pr.violations().stream())
                .flatMap(v -> v.getSuccessCriteria().stream())
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new CriterionSummary(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(CriterionSummary::criterion, WcagCriterion.SPEC_ORDER))
                .toList();

        long bestPractice = pages.stream()
                .flatMap(pr -> pr.violations().stream())
                .filter(v -> v.getSuccessCriteria().isEmpty())
                .count();

        return new Report(pages, total, categories, criteria, bestPractice);
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        scanJobRepository.findFirstByIsExampleTrue().ifPresent(job -> {
            model.addAttribute("exampleJob", job);
            // the home page shows the curated example scan in the real report window, so the
            // picture of the product is the product
            model.addAttribute("exampleReport", buildReport(job.getId()));
        });

        User user = currentUser(authentication);
        if (user != null) {
            List<ScanJob> jobs = scanJobRepository.findByOwnerId(user.getId());
            jobs.sort(Comparator.comparing(ScanJob::getCreatedAt).reversed());
            model.addAttribute("jobs", jobs);
        }
        return "scans";
    }

    @GetMapping("/how-it-works")
    public String howItWorks(Model model) {
        scanJobRepository.findFirstByIsExampleTrue().ifPresent(job -> model.addAttribute("exampleJob", job));
        return "how-it-works";
    }

    @PostMapping("/guest-scan")
    public String guestScan(@RequestParam String url) {
        ScanJob job = scanService.enqueue(url);
        return "redirect:/scans/" + job.getId() + "/report";
    }

    @GetMapping("/scans/{id}/report")
    public String scanReport(@PathVariable UUID id, Authentication authentication, Model model) {
        ScanJob job = scanJobRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        User viewer = currentUser(authentication);
        if (!job.isViewableBy(viewer)) {
            if (job.isAnonymous()) {
                // already shown once - that's expected, not an access violation
                return "scan-expired";
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        boolean isTerminal = job.getStatus() == ScanStatus.DONE || job.getStatus() == ScanStatus.FAILED;
        if (job.isAnonymous() && isTerminal) {
            scanService.recordAnonymousView(id);
        }

        model.addAttribute("job", job);
        model.addAttribute("report", buildReport(id));
        return "scan-detail";
    }
}
