package hudson.plugins.performance;

import hudson.model.AbstractBuild;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;

public class ThroughputChart {

    protected static JFreeChart createThroughputChart(final CategoryDataset dataSet) {
        final JFreeChart chart = ChartFactory.createLineChart(
                Messages.ProjectAction_Throughput(), // chart title
                null, // unused
                Messages.ProjectAction_RequestsPerSeconds(), // range axis label
                dataSet, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
        );

        final LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.BOTTOM);

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseStroke(new BasicStroke(4.0f));
        ColorPalette.apply(renderer);

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

        return chart;
    }

    public static void render(
            final StaplerRequest request, final StaplerResponse response,
            final String performanceReportNameFile, final java.util.List<AbstractBuild> buildsRange)
            throws IOException {
        final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        for (final AbstractBuild<?, ?> build : buildsRange) {
            final PerformanceBuildAction performanceBuildAction = build.getAction(PerformanceBuildAction.class);
            if (performanceBuildAction == null) {
                continue;
            }

            final PerformanceReport performanceReport = performanceBuildAction
                    .getPerformanceReportMap().getPerformanceReport(performanceReportNameFile);
            if (performanceReport == null) {
                continue;
            }

            final ThroughputReport throughputReport = new ThroughputReport(performanceReport);
            final ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
            dataSetBuilder.add(throughputReport.get(), Messages.ProjectAction_RequestsPerSeconds(), label);
        }

        ChartUtil.generateGraph(request, response, createThroughputChart(dataSetBuilder.build()), 400, 200);
    }
}
