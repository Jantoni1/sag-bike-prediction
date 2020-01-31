package com.sag.eiti.vaadin_views;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.sag.eiti.actors.BikeDemandActor;
import com.sag.eiti.actors.PredictionModelActor;
import com.sag.eiti.config.SpringProps;
import com.sag.eiti.entity.PredictedTripsPerHour;
import com.sag.eiti.service.BikeDemandPredictionService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
@Push
public class MainView extends VerticalLayout {

    private DatePicker startDatePicker;

    private TimePicker startTimePicker;

    private TimePicker stopTimePicker;

    private Button updateChartButton;

    private Chart chart;

    private final ActorSystem system;

    private BikeDemandPredictionService bikeDemandPredictionService;

    public MainView(@Autowired ActorSystem system, @Autowired BikeDemandPredictionService bikeDemandPredictionService) {
        this.system = system;
        this.bikeDemandPredictionService = bikeDemandPredictionService;

        initUi();

        HorizontalLayout horizontalLayout = new HorizontalLayout(startDatePicker, startTimePicker, stopTimePicker, updateChartButton);
        horizontalLayout.addClassName("centered-content");

        HorizontalLayout topBar = new HorizontalLayout(new Text("Bergen City Bike Demand Prediction"));
        topBar.addClassName("top-bar");

        add(topBar, horizontalLayout);
        addClassName("remove-padding");
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
        startDatePicker.setPlaceholder("Date within this month");

        LocalDate now = LocalDate.now();

        startDatePicker.setMin(now.withDayOfMonth(1));
        startDatePicker.setMax(now.withDayOfMonth(now.lengthOfMonth()));

        startDatePicker.addValueChangeListener(AbstractField.ComponentValueChangeEvent::getValue);
    }

    private void initializeUpdateChartButton() {

        updateChartButton = new Button("Show", e -> resetChart());

        updateChartButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        updateChartButton.addClickShortcut(Key.ENTER);
    }

    private void resetChart() {

        ListSeries predictedRidesSeries = new ListSeries("Predicted", new ArrayList<>());
        ListSeries ridesSeries = new ListSeries("Actual rides", new ArrayList<>());
        ListSeries trainedModelSeries = new ListSeries("Quick Prediction", new ArrayList<>());

        List<Series> usedSeries = new ArrayList<>();
        usedSeries.add(predictedRidesSeries);

        var predictionTimeSpan = getRequestedTimeSpan(predictedRidesSeries);

        var quickPrediction = bikeDemandPredictionService.getPrediction(predictionTimeSpan);
        if(!quickPrediction.isEmpty()) {
            quickPrediction.forEach(trainedModelSeries::addData);
            usedSeries.add(trainedModelSeries);
        }

        ActorRef predictionBikeRidesManager = system.actorOf(SpringProps.create(system, BikeDemandActor.class));
        predictionBikeRidesManager.tell(predictionTimeSpan, ActorRef.noSender());

        if(predictionTimeSpan.getTimeSpanEnd().isBefore(LocalDateTime.now().atOffset(ZoneOffset.UTC).minusDays(1))) {
            usedSeries.add(ridesSeries);
            var historicalTimeSpan = getHistoricalRidesData(ridesSeries);

            ActorRef historicalBikeRidesManager = system.actorOf(SpringProps.create(system, BikeDemandActor.class));
            historicalBikeRidesManager.tell(historicalTimeSpan, ActorRef.noSender());
        }

        updateChart(predictionTimeSpan, usedSeries);
    }

    private void updateChart(BikeDemandActor.BikePredictionRequest predictionTimeSpan, List<Series> chartSeries) {
        Chart oldChart = chart;
        chart = createChart(predictionTimeSpan, chartSeries);
        replace(oldChart, chart);
    }

    private BikeDemandActor.BikePredictionRequest getRequestedTimeSpan(ListSeries predictedRidesSeries) {
        return new BikeDemandActor.BikePredictionRequest(getStartDateTime(), getEndDateTime(), this, predictedRidesSeries);
    }

    private BikeDemandActor.BikeHistoricalRidesRequest getHistoricalRidesData(ListSeries ridesSeries) {
        return new BikeDemandActor.BikeHistoricalRidesRequest(getStartDateTime(), getEndDateTime(), this, ridesSeries);
    }

    private OffsetDateTime getStartDateTime() {
        return LocalDateTime
                .of(startDatePicker.getValue(), startTimePicker.getValue())
                .atOffset(ZoneOffset.UTC);
    }

    private OffsetDateTime getEndDateTime() {
        return LocalDateTime
                .of(startDatePicker.getValue(), stopTimePicker.getValue())
                .atOffset(ZoneOffset.UTC);
    }

    private Chart createChart(BikeDemandActor.BikePredictionRequest predictionTimeSpan, List<Series> chartSeries) {
        Chart chart = new Chart(ChartType.SPLINE);

        Configuration conf = chart.getConfiguration();

        XAxis xAxis = new XAxis();
        xAxis.setCategories(predictionTimeSpan.toHours().stream()
                .map(OffsetDateTime::getHour)
                .map(hourInt -> String.format("%02d:00", hourInt)).toArray(String[]::new));

        xAxis.setPlotBands();

        chart.getConfiguration().addxAxis(xAxis);

        conf.setTitle(new Title("Bike usage"));

        Legend legend = new Legend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setAlign(HorizontalAlign.LEFT);
        legend.setFloating(true);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setX(150);
        legend.setY(100);
        conf.setLegend(legend);

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

        chartSeries.forEach(series -> chart.getConfiguration().addSeries(series));

        return chart;
    }

    public void addPredictedSeries(List<PredictedTripsPerHour> bikePredictions) {
        getUI().ifPresent(ui -> ui.access(() -> {


        }));
    }

    public void addActualRidesSeries(List<BikeDemandActor.BikeHistoricalRidesResponse> historicalBikeRides) {
        getUI().ifPresent(ui -> ui.access(() -> {

            ListSeries actualRidesCount = new ListSeries("Actual rides", historicalBikeRides.stream()
                    .map(BikeDemandActor.BikeHistoricalRidesResponse::getNumberOfRides)
                    .collect(Collectors.toList()));

            chart.getConfiguration().addSeries(actualRidesCount);
        }));
    }

}
