/* The report window (fragments/report-window.html): the page list switches the open panel, and the category
   chips (.chips[data-rw-filter]) narrow every panel to one category. Without this script every panel shows,
   one under the other, and the list links jump to them. */
(function () {
    "use strict";

    function setup(win) {
        var links = win.querySelectorAll("[data-rw-to]");
        var panels = win.querySelectorAll(".rw__panel");
        if (!panels.length) return;
        win.classList.add("is-js");

        function open(id, focus) {
            panels.forEach(function (p) { p.classList.toggle("is-on", p.id === id); });
            links.forEach(function (a) {
                if (a.getAttribute("data-rw-to") === id) a.setAttribute("aria-current", "true");
                else a.removeAttribute("aria-current");
            });
            var openPane = win.querySelector(".rw__open");
            if (openPane) openPane.scrollTop = 0;
            if (focus) {
                var h = document.getElementById(id);
                if (h) { var t = h.querySelector(".rw__h"); if (t) { t.setAttribute("tabindex", "-1"); t.focus({ preventScroll: true }); } }
            }
        }

        links.forEach(function (a) {
            a.addEventListener("click", function (e) {
                e.preventDefault();
                open(a.getAttribute("data-rw-to"), !win.classList.contains("rw--demo"));
            });
        });
    }

    function filters() {
        var bar = document.querySelector("[data-rw-filter]");
        if (!bar) return;
        var chips = bar.querySelectorAll("[data-category]");
        function apply(cat) {
            document.querySelectorAll(".rw__panel").forEach(function (panel) {
                var rows = panel.querySelectorAll("tr[data-category]");
                if (!rows.length) return;
                var any = false;
                rows.forEach(function (r) {
                    var on = !cat || r.getAttribute("data-category") === cat;
                    r.hidden = !on;
                    if (on) any = true;
                });
                panel.classList.toggle("is-filtered-empty", !!cat && !any);
            });
        }
        chips.forEach(function (chip) {
            chip.addEventListener("click", function () {
                var was = chip.getAttribute("aria-pressed") === "true";
                chips.forEach(function (c) { c.setAttribute("aria-pressed", "false"); });
                if (!was) chip.setAttribute("aria-pressed", "true");
                apply(was ? null : chip.getAttribute("data-category"));
            });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll("[data-rw]").forEach(setup);
        filters();
    });
})();
