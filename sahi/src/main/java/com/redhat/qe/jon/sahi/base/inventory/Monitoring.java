package com.redhat.qe.jon.sahi.base.inventory;

import com.redhat.qe.jon.sahi.tasks.SahiTasks;
import com.redhat.qe.jon.sahi.tasks.Timing;
import net.sf.sahi.client.ElementStub;

import java.util.List;
import java.util.logging.Logger;

public class Monitoring extends ResourceTab {

    public Monitoring(SahiTasks tasks, Resource resource) {
        super(tasks, resource);
    }

    @Override
    protected void navigate() {
        navigateUnderResource("Monitoring");
    }

    /**
     * selects <b>Tables</b> subtab and returns helper object
     *
     * @return tables subtab
     * @deprecated tables are no longer present in JON 3.2
     */
    @Deprecated
    public Tables tables() {
        navigateUnderResource("Monitoring/Tables");
        return new Tables(tasks);
    }

    /**
     * selects <b>Traits</b> subtab and returns helper object
     *
     * @return tables subtab
     */
    public Tables traits() {
        navigateUnderResource("Monitoring/Traits");
        return new Tables(tasks);
    }

    /**
     * selects <b>Schedules</b> subtab and returns helper object
     *
     * @return schedules subtab
     */
    public Schedules schedules() {
        navigateUnderResource("Monitoring/Schedules");
        return new Schedules(tasks);
    }

    /**
     * selects <b>Metrics</b> subtab and returns helper object
     *
     * @return metrics subtab
     */
    public Tables metrics() {
        navigateUnderResource("Monitoring/Metrics");
        return new Tables(tasks);
    }

    public static class Schedules {
        private final SahiTasks tasks;
        private final Logger log = Logger.getLogger(this.getClass().getName());
        private final MetricsFinder metricsFinder;

        private Schedules(SahiTasks tasks) {
            this.tasks = tasks;
            this.metricsFinder = new MetricsFinder(tasks);
        }


        /**
         * sets collection interval for givem metric
         *
         * @param metric
         * @param interval in minutes
         */
        public void setInterval(String metric, String interval) {
            tasks.xy(metricsFinder.getMetricCell(metric), 3, 3).click();
            ElementStub textbox = tasks.textbox("interval");
            textbox.setValue(interval);
            for (ElementStub setButton : tasks.cell("Set").collectSimilar()) {
                String setButtonClass = setButton.getAttribute("class");
                if ("buttonDisabled".equals(setButtonClass)) {
                    log.finer("Clicking again on the " + metric + " in order to enable the set button");
                    tasks.xy(metricsFinder.getMetricCell(metric), 3, 3).click();
                }
                tasks.xy(setButton, 3, 3).click();
            }
        }

        /**
         * gets interval for metric
         *
         * @param metric name
         * @return collection interval
         */
        public String getInterval(String metric) {
            tasks.waitFor(Timing.TIME_1S);
            return tasks.cell(4).in(metricsFinder.getMetricCell(metric).parentNode("tr")).getText();
        }

        /**
         * enables metric defined by name
         *
         * @param metric
         */
        public void enable(String metric) {
            tasks.xy(metricsFinder.getMetricCell(metric), 3, 3).click();
            for (ElementStub enable : tasks.cell("Enable").collectSimilar()) {
                enable.click();
            }
        }

        /**
         * disables metric defined by name
         *
         * @param metric
         */
        public void disable(String metric) {
            tasks.xy(metricsFinder.getMetricCell(metric), 3, 3).click();
            for (ElementStub disable : tasks.cell("Disable").collectSimilar()) {
                disable.click();
            }
        }
    }

    public static class Tables {
        private final SahiTasks tasks;
        private final Logger log = Logger.getLogger(this.getClass().getName());
        private final MetricsFinder metricsFinder;

        private Tables(SahiTasks tasks) {
            this.tasks = tasks;
            this.metricsFinder = new MetricsFinder(tasks);
        }

        /**
         * @param metricName
         * @return true if metric with given name is present in metrics table
         */
        public boolean containsMetric(String metricName) {
            ElementStub metricCell = metricsFinder.getMetricCellWithScrolling(metricName);
            return (metricCell != null && metricCell.isVisible());
        }

        public void refresh() {
            tasks.reloadPage();
        }

        /**
         * checks whether given metric row contains given value
         *
         * @param metric name of metric
         * @param value  to be contained within metric row
         * @return true if given value is contained in metric row
         */
        public boolean containsMetricRowValue(String metric, String value) {
            ElementStub metricCell = metricsFinder.getMetricCell(metric);
            ElementStub row = metricCell.parentNode("tr");
            return row.getText().contains(value);
        }

        /**
         * Retrieves measured value for given metric
         *
         * @param metric           name of metric
         * @param valueColumnIndex index in metric table (note that 0 will return metric
         *                         name)
         * @return String value found in given column for given metric
         */
        public String getMetricRowValue(String metric, int valueColumnIndex) {
            ElementStub metricCell = metricsFinder.getMetricCell(metric);
            ElementStub row = metricCell.parentNode("tr");
            return tasks.cell(valueColumnIndex).in(row).getText();
        }
    }

    /**
     * Helper class for finding proper metrics in tables used by Tables and Schedules classes.
     */
    private static class MetricsFinder {
        private final SahiTasks tasks;
        private final Logger log = Logger.getLogger(this.getClass().getName());

        private MetricsFinder(SahiTasks tasks) {
            this.tasks = tasks;
        }

        private ElementStub getMetricCell(String metricName) {
            ElementStub metricCell = getMetricCellWithScrolling(metricName);
            if (metricCell == null || !metricCell.isVisible()) {
                throw new RuntimeException("Unable to find metric cell called ["
                        + metricName + "]");
            } else {
                return metricCell;
            }
        }

        /**
         * Finds metric cell for the provided metric name and returns it. If such cell is not found, null is returned.
         */
        private ElementStub getMetricCellWithScrolling(String metricName) {
            List<ElementStub> tables = tasks.table("listTable")
                    .collectSimilar();
            log.fine("listTable count = " + tables.size());
            for (ElementStub table : tables) {
                if (table.isVisible()) {
                    ElementStub metricCell = tasks.cell(metricName).in(table);
                    int maxScrollAtempts = 10;
                    int scrollAttempts = 0;
                    // it is possible that the element is in the page, just it isn't in the visible area, lets try scrolling
                    if (getVScrollButton("track") != null && !metricCell.isVisible()) { // scroller exists, lets try scrolling to find
                        log.fine("metric " + metricName + " is not visible, reloading page and starting scrolling from top");
                        tasks.reloadPage(); // reloading whole page => making sure we start at top,

                        metricCell = tasks.cell(metricName).in(table);
                        // whether element exists on the page
                        while (!metricCell.isVisible() && (scrollAttempts < maxScrollAtempts)) {
                            scrollDown(10);
                            scrollAttempts++;
                            metricCell = tasks.cell(metricName).in(table);
                        }
                    }
                    if (metricCell.isVisible()) {
                        return metricCell;
                    }
                }
            }
            return null;
        }

        /**
         * clicks on bottom scroll arrow
         *
         * @param clicks - how many times to click
         */
        public void scrollDown(int clicks) {
            ElementStub scroll = getVScrollButton("end");
            for (int i = 0; i < clicks; i++) {
                if (scroll != null && scroll.exists() && scroll.isVisible()) {
                    tasks.xy(scroll.parentNode(), 3, 3).click();
                    log.fine("Clicked scroll arrow");
                    tasks.waitFor(Timing.TIME_1S);
                } else {
                    log.warning("Scroll arrow not found!");
                }
            }
        }

        /**
         * clicks once on bottom scroll arrow
         */
        public void scrollDown() {
            scrollDown(1);
        }

        /**
         * clicks on upper scroll arrow
         *
         * @param clicks - how many times to click
         */
        public void scrollUp(int clicks) {
            ElementStub scroll = getVScrollButton("start");
            for (int i = 0; i < clicks; i++) {
                if (scroll != null && scroll.exists() && scroll.isVisible()) {
                    tasks.xy(scroll, 3, 3).click();
                    log.fine("Clicked scroll arrow");
                    tasks.waitFor(Timing.TIME_1S);
//                scroll = getVScrollButton("Over_start");
                } else {
                    log.warning("Scroll arrow not found!");
                }
            }
        }

        /**
         * clicks once on upper scroll arrow
         */
        public void scrollUp() {
            scrollUp(1);
        }


        private ElementStub getVScrollButton(String type) {
            List<ElementStub> scrolls = tasks.image("/.*vscroll_" + type + ".*/").collectSimilar();
            if (scrolls.size() > 0) {
                return scrolls.get(scrolls.size() - 1);
            }
            return null;
        }

        private void refresh() {
            tasks.cell("Refresh").click();
        }

    }


}
