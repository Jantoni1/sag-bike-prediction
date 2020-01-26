package com.sag.eiti.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.sag.eiti.config.interfaces.Actor;
import com.sag.eiti.dto.WeatherForecast;
import com.sag.eiti.service.DarkSkyWeatherService;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Comparator;

@Actor
public class WeatherCaster extends AbstractActor {

    DarkSkyWeatherService darkSkyWeatherService;

    public WeatherCaster(DarkSkyWeatherService darkSkyWeatherService) {
        this.darkSkyWeatherService = darkSkyWeatherService;
    }

    private final Integer BERGEN_CITY_ID = 3161732;

    private static final String CURRENT_WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";

    private static final String FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast";

    private final String CITY = "Warsaw,pl";

    private final static String HISTORY_API = "https://api.darksky.net/forecast/960b80074098b4e0739163acebdbd1d5/60.39299,5.32415,%d,units=ca";


//    https://api.darksky.net/forecast/960b80074098b4e0739163acebdbd1d5/60.39299,5.32415,1577888379,units=ca
    //http://history.openweathermap.org/data/2.5/history/city?id=3161732&type=hourAPPID=00967a47a6f379a2ec97f1b2e6a34053 todo

    private final String API_KEY = "APPID=00967a47a6f379a2ec97f1b2e6a34053";

    private String getCurrentWeatherApiUrl() {
        return String.format("%s?q=%s&%s&units=metric", CURRENT_WEATHER_URL, CITY, API_KEY);
    }

    private String getForecastApiUrl() {
        return String.format("%s?q=%s&%s&units=metric", FORECAST_URL, CITY, API_KEY);
    }

    @Value
    public final static class WeatherForecastRequest {
        LocalDateTime hour;
        ActorRef from;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(WeatherForecastRequest.class, this::onCastRequestReceived)
                .build();
    }

    private void onCastRequestReceived(WeatherForecastRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<WeatherForecast> response = restTemplate.getForEntity(getForecastApiUrl(), WeatherForecast.class);
        if(response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            response.getBody().getThreeHourForecastList()
                    .stream()
                    .min(Comparator.comparingLong(weatherDetails -> Math
                            .abs(request.hour.getSecond() - weatherDetails.getTime().getSecond())))
                    .ifPresent(weatherDetails -> System.out.println(String.format("%s, %s", weatherDetails.getTime(), weatherDetails.getTemperature())));
        }
        getContext().stop(getSelf());
    }

}
