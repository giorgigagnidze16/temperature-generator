package com.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class Main {
  public static final String QUEUE = "thermostat_creation_queue";
  public static final String EXCHANGE = "thermostat_creation_exc";
  public static final String ROUTING_KEY = "thermostat_routing_key";
  private static final String API = "http://localhost:8080/api/noauth";

  public static void main(String[] args) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setPort(5672);
    factory.setUsername("guest");
    factory.setPassword("guest");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE, "direct", true);
    channel.queueDeclare(QUEUE, true, false, false, null);
    channel.queueBind(QUEUE, EXCHANGE, ROUTING_KEY);

    DefaultConsumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body, StandardCharsets.UTF_8);
        if (message.contains(":")) {
          long id = Long.parseLong(message.replaceAll("[^0-9]", ""));

          HttpClient httpClient = HttpClient.newBuilder().build();
          HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(API))
              .POST(HttpRequest.BodyPublishers.ofString(
                  new ObjectMapper().writeValueAsString(new Thermostat(id, getRandomTemperature()))))
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .build();
          HttpResponse<String> response = null;
          try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          if (response != null && response.statusCode() == HttpStatus.OK.value()) {
            System.out.println("Updated successfully");
          }

        }
      }
    };

    channel.basicConsume(QUEUE, true, consumer);
    System.out.println("Waiting for messages...");
  }


  public static double getRandomTemperature() {
    double min = -20;
    double max = 120;
    double range = max - min;
    double scaled = Math.random() * range;
    return scaled + min;
  }
}
