package com.assignment.carstore.controller;

import com.assignment.carstore.components.SessionDataStorage;
import com.assignment.carstore.domain.ChargeSession;
import com.assignment.carstore.domain.CounterSummary;
import com.assignment.carstore.exceptions.ChargingSessionException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * The service which represents a store for car charging session entities.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
@Controller
@RequestMapping(value = "/chargingSessions", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChargeController {

  private SessionDataStorage sessionDataStorage;

  public ChargeController(SessionDataStorage sessionDataStorage) {
    this.sessionDataStorage = sessionDataStorage;
  }

  /**
   * Submit a new charging session for the station.
   *
   * @param stream
   * @return
   */
  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<?> submitSession(InputStream stream) {
    JSONObject jsonObject;
    try {
      jsonObject = getJsonObjectFromStream(stream);
      stationIdCheck(jsonObject);
    } catch (ChargingSessionException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    ChargeSession session = new ChargeSession(jsonObject.get("stationId").toString(), LocalDateTime.now());
    return new ResponseEntity<>(sessionDataStorage.submitSession(session), HttpStatus.OK);
  }

  /**
   * Stop charging session.
   *
   * @param id
   * @return
   */
  @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
  @ResponseBody
  public ResponseEntity<?> stopSession(@PathVariable("id") UUID id) {
    ChargeSession chargeSession;
    try {
      chargeSession = sessionDataStorage.stopSession(id);
    } catch (ChargingSessionException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(chargeSession, HttpStatus.OK);
  }

  /**
   * Retrieve all charging sessions.
   * @return
   */
  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<Collection<ChargeSession>> retrieveAllSessions() {
    return new ResponseEntity<>(sessionDataStorage.retrieveAllSessions(), HttpStatus.OK);
  }

  /**
   * Retrieve a summary of submitted charging sessions including:
   * - totalCount - total number of charging session updates for the last minute;
   * - startedCount - total number of started charging sessions for the last minute;
   * - stoppedCount - total number of stopped charging sessions for the last minute.
   *
   * @return
   */
  @RequestMapping(method = RequestMethod.GET, value = "/summary")
  @ResponseBody
  public ResponseEntity<CounterSummary> retrieveSummarySubmittedSessions() {
    return new ResponseEntity<>(sessionDataStorage.retrieveSummarySubmittedSessions(), HttpStatus.OK);
  }

  /**
   * Check if request body contains 'stationId' key.
   *
   * @param jsonObject
   * @throws ChargingSessionException
   */
  private static void stationIdCheck(JSONObject jsonObject) throws ChargingSessionException {
    if (!Objects.requireNonNull(jsonObject).containsKey("stationId")) {
      throw new ChargingSessionException("Field 'stationId' is not found in request body", HttpStatus.BAD_REQUEST.value());
    }
  }

  /**
   * Retrieve JSON object from an input stream.
   *
   * @param stream
   * @return
   * @throws ChargingSessionException
   */
  private static JSONObject getJsonObjectFromStream(InputStream stream) throws ChargingSessionException {
    JSONObject jsonObject;
    try {
      JSONParser jsonParser = new JSONParser();
      jsonObject = (JSONObject) jsonParser.parse(
              new InputStreamReader(stream, StandardCharsets.UTF_8));
    } catch (IOException | ParseException e) {
      throw new ChargingSessionException("Request body is invalid or empty.", HttpStatus.BAD_REQUEST.value(), e);
    }
    return jsonObject;
  }
}
