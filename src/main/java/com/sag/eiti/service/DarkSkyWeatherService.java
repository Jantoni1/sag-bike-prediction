package com.sag.eiti.service;

import com.sag.eiti.dto.dark_sky.HistoricalHourWeatherMeasurement;
import com.sag.eiti.dto.dark_sky.HistoricalWeather;
import com.sag.eiti.dto.dark_sky.WeatherForecast;
import com.sag.eiti.entity.TripsPerHourHistorical;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DarkSkyWeatherService {

    private final static String DARK_SKY_CORE_URL = "https://api.darksky.net/forecast/960b80074098b4e0739163acebdbd1d5/60.39299,5.32415";

    private final static String DARK_SKY_URL = DARK_SKY_CORE_URL.concat(",%10d?units=ca&exclude=daily,currently");

    private final static String DARK_SKY_FORECAST_URL = DARK_SKY_CORE_URL.concat("?units=ca&exclude=daily,currently");

    public List<TripsPerHourHistorical> getHistoricalWeatherForDay(List<BergenCityBikesService.TimeSpanRidesDetails> requestedTimeSpans) {
        return requestedTimeSpans.stream().collect(Collectors.groupingBy(timeSpan -> timeSpan.getRequestedHour().withHour(0))).entrySet()
                .stream().flatMap(entry -> {
                    var historicalWeather = getHistoricalWeatherForTimeSpan(entry.getKey()).getHourly().getData().stream()
                            .collect(Collectors.toMap(HistoricalHourWeatherMeasurement::getOffsetDateTime, Function.identity()));

                    return entry.getValue().stream().map(ridesDetails -> {
                        var weatherData = historicalWeather.get(ridesDetails.getRequestedHour());
                        return new TripsPerHourHistorical(null, ridesDetails.getRequestedHour().toInstant(), (int) ridesDetails.getRidesCount(), weatherData.getTemperature());
                    });
                }).collect(Collectors.toList());
    }

    private HistoricalWeather getHistoricalWeatherForTimeSpan(OffsetDateTime requestedDay) {
        var historicalWeather = new RestTemplate().getForObject(getDarkSkyUrl(requestedDay), HistoricalWeather.class);
        if (historicalWeather != null && historicalWeather.getHourly() != null) {
            historicalWeather.getHourly().getData().forEach(measurement -> measurement.setOffsetDateTime(requestedDay.getOffset()));
        }
        return historicalWeather;
    }

    public Map<OffsetDateTime, Double> getWeatherForecast(List<OffsetDateTime> requestedForecastHours) {
        var currentTime = LocalDateTime.now().atOffset(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);

        var forecastTemperatures = getForecastTemperatures(currentTime, requestedForecastHours);
        var historicalTemperatures = getHistoricalTemperatures(currentTime, requestedForecastHours);

        forecastTemperatures.putAll(historicalTemperatures);

        return forecastTemperatures;
    }

    private Map<OffsetDateTime, Double> getForecastTemperatures(OffsetDateTime currentTime, List<OffsetDateTime> requestedHours) {
        List<OffsetDateTime> requestedPastHours = requestedHours.stream().filter(hour -> !hour.isBefore(currentTime)).collect(Collectors.toList());

        var forecastWeather = new RestTemplate().getForObject(DARK_SKY_FORECAST_URL, WeatherForecast.class);
        if (forecastWeather != null && forecastWeather.getHourly() != null) {
            return forecastWeather.getHourly().getData().stream()
                    .peek(measurement -> measurement.setOffsetDateTime(ZoneOffset.UTC))
                    .filter(weatherMeasurement -> requestedPastHours.contains(weatherMeasurement.getOffsetDateTime()))
                    .collect(Collectors.toMap(HistoricalHourWeatherMeasurement::getOffsetDateTime, HistoricalHourWeatherMeasurement::getTemperature));
        }
        return new HashMap<>();
    }

    private Map<OffsetDateTime, Double> getHistoricalTemperatures(OffsetDateTime currentTime, List<OffsetDateTime> requestedHours) {
        List<OffsetDateTime> requestedPastHours = requestedHours.stream().filter(hour -> hour.isBefore(currentTime)).collect(Collectors.toList());

        if(!requestedPastHours.isEmpty()) {
            var historicalWeather = new RestTemplate().getForObject(getDarkSkyUrl(requestedPastHours.get(0)), HistoricalWeather.class);
            if (historicalWeather != null && historicalWeather.getHourly() != null) {
                return historicalWeather.getHourly().getData().stream()
                        .peek(measurement -> measurement.setOffsetDateTime(ZoneOffset.UTC))
                        .filter(weatherMeasurement -> requestedPastHours.contains(weatherMeasurement.getOffsetDateTime()))
                        .collect(Collectors.toMap(HistoricalHourWeatherMeasurement::getOffsetDateTime, HistoricalHourWeatherMeasurement::getTemperature));
            }
        }
        return new HashMap<>();

    }


    private static String getDarkSkyUrl(OffsetDateTime dayDate) {
        return String.format(DARK_SKY_URL, dayDate.toEpochSecond());
    }
}
