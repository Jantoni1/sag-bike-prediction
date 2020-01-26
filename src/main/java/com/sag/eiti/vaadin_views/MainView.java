package com.sag.eiti.vaadin_views;

import com.sag.eiti.entity.PredictedTripsPerHour;
import com.sag.eiti.service.BergenCityBikesService;
import com.sag.eiti.service.BikePredictionService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.*;
import java.util.stream.Collectors;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */
@Route()
@PWA(name = "Vaadin Application",
        shortName = "Vaadin App",
        description = "This is an example Vaadin application.",
        enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {

    private DatePicker startDatePicker;

    private DatePicker stopDatePicker;

    private TimePicker startTimePicker;

    private TimePicker stopTimePicker;

    private Button updateChartButton;

    private Chart chart;

    private BikePredictionService bikePredictionService;

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     *
     * @param service The message service. Automatically injected Spring managed bean.
     */
    public MainView(@Autowired BikePredictionService service) {
        this.bikePredictionService = service;

        initUi();

//        addClassName("centered-content");

        HorizontalLayout horizontalLayout = new HorizontalLayout(startDatePicker, startTimePicker, stopTimePicker, updateChartButton);

        add(horizontalLayout);
    }

    private void initUi() {
        initializeStartDatePicker();
        initializeStartTimePicker();
        initializeStopTimePicker();
        initializeUpdateChartButton();
    }

    private void initializeStartTimePicker() {
        startTimePicker = new TimePicker();
        startTimePicker.setStep(Duration.ofHours(1));
        startTimePicker.addValueChangeListener(event -> {
            stopTimePicker.setMin(event.getValue().toString());
            stopTimePicker.setValue(event.getValue());
        });
    }

    private void initializeStopTimePicker() {
        stopTimePicker = new TimePicker();
        stopTimePicker.setStep(Duration.ofHours(1));
    }

    private void initializeStartDatePicker() {
        startDatePicker = new DatePicker();
        startDatePicker.setLabel("Select a day within this month");
        startDatePicker.setPlaceholder("Date within this month");

        LocalDate now = LocalDate.now();

        startDatePicker.setMin(now.withDayOfMonth(1));
        startDatePicker.setMax(now.withDayOfMonth(now.lengthOfMonth()));

        startDatePicker.addValueChangeListener(event -> event.getValue());
    }

    private void initializeUpdateChartButton() {
        // Button click listeners can be defined as lambda expressions
        updateChartButton = new Button("Show", e -> updateChart());

        // Theme variants give you predefined extra styles for components.
        // Example: Primary button is more prominent look.
        updateChartButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // You can specify keyboard shortcuts for buttons.
        // Example: Pressing enter in this view clicks the Button.
        updateChartButton.addClickShortcut(Key.ENTER);
    }

    private void initializeStopDatePicker() {
        stopDatePicker = new DatePicker();
        stopDatePicker.setPlaceholder("Date within this month");

        LocalDate now = LocalDate.now();

        stopDatePicker.setMin(now.withDayOfMonth(1));
        stopDatePicker.setMax(now.withDayOfMonth(now.lengthOfMonth()));

        stopDatePicker.addValueChangeListener(event -> event.getValue());
    }

    private void updateChart() {
        Chart oldChart = chart;
        chart = createChart();
        replace(oldChart, chart);
    }

    private BergenCityBikesService.RequestedTimeSpan getRequestedTimeSpan() {
        OffsetDateTime startDateTime = LocalDateTime
                .of(startDatePicker.getValue(), startTimePicker.getValue())
                .atOffset(ZoneOffset.UTC);
        OffsetDateTime stopDateTime = LocalDateTime
                .of(startDatePicker.getValue(), stopTimePicker.getValue())
                .atOffset(ZoneOffset.UTC);
        return new BergenCityBikesService.RequestedTimeSpan(startDateTime, stopDateTime);
    }

    private Chart createChart() {
        var requestedTimeSpan = getRequestedTimeSpan();
        var chartData = bikePredictionService.getPrediction(requestedTimeSpan);

        Chart chart = new Chart(ChartType.AREASPLINE);

        Configuration conf = chart.getConfiguration();

        conf.setTitle(new Title("Bike usage"));

        Legend legend = new Legend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setAlign(HorizontalAlign.LEFT);
        legend.setFloating(true);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setX(150);
        legend.setY(100);
        conf.setLegend(legend);

        XAxis xAxis = new XAxis();
        xAxis.setCategories(chartData.stream()
                .map(predictedTripsPerHour -> predictedTripsPerHour.getPredictedHourStart().atOffset(ZoneOffset.UTC).getHour())
                .map(hourInt -> String.format("%02d:00", hourInt)).toArray(String[]::new));

//        PlotBand plotBand = new PlotBand(4.5, 6.5);
//        plotBand.setZIndex(1);
        xAxis.setPlotBands();

//        XAxis xAxis = new XAxis();
//        xAxis.setCategories("Monday", "Tuesday", "Wednesday",
//                "Thursday", "Friday", "Saturday", "Sunday");
//        PlotBand plotBand = new PlotBand(4.5, 6.5);
//        plotBand.setZIndex(1);
//        xAxis.setPlotBands(plotBand);
//        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle(new AxisTitle("Number of bikes"));
        conf.addyAxis(yAxis);

        Tooltip tooltip = new Tooltip();
        // Customize tooltip formatting
        tooltip.setShared(true);
        tooltip.setValueSuffix(" bikes");
        conf.setTooltip(tooltip);

        PlotOptionsArea plotOptions = new PlotOptionsArea();
        conf.setPlotOptions(plotOptions);


        chart.getConfiguration().addxAxis(xAxis);

        ListSeries o = new ListSeries("Predicted", chartData.stream()
                .map(PredictedTripsPerHour::getPredictedRides)
                .collect(Collectors.toList()));

        chart.getConfiguration().addSeries(o);

        if(requestedTimeSpan.getTimeSpanEnd().isBefore(LocalDateTime.now().atOffset(ZoneOffset.UTC).minusDays(1))) {
            var historicalBikeRidesData =  bikePredictionService.getHistoricalRidesData(requestedTimeSpan);

            ListSeries actualRidesCount = new ListSeries("Actual rides", historicalBikeRidesData.stream()
                    .map(BikePredictionService.BikeData::getNumberOfRides)
                    .collect(Collectors.toList()));

            chart.getConfiguration().addSeries(actualRidesCount);
        }

//        var data = bikePredictionService.getData();

//        ListSeries o = new ListSeries("Predicted", data.stream().map(PredictedTripsPerHour::getPredictedRides)
//                .limit(7).collect(Collectors.toList()));
        // You can also add values separately
//        o.addData(12);
//        conf.addSeries(o);
//        conf.addSeries(new ListSeries("Used", 1, 3, 4, 3, 3, 5, 4));
        return chart;
    }

}
