package com.assignment.carstore.components.impl;

import com.assignment.carstore.components.DataStorage;
import com.assignment.carstore.domain.ChargeSession;
import com.assignment.carstore.domain.CounterSummary;
import com.assignment.carstore.domain.StatusEnum;
import com.assignment.carstore.exceptions.ChargingSessionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Test for SessionDataStorageImpl class.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
class SessionDataStorageImplTest {

  private DataStorage<UUID, ChargeSession, CounterSummary> dataStorage;

  @BeforeEach
  public void init() {
    dataStorage = new SessionDataStorageImpl();
  }

  @AfterEach
  public void destroy() {
    dataStorage = null;
  }

  @Test
  void submitSession() {
    ChargeSession session = new ChargeSession("ABC-12345", LocalDateTime.now());
    assertEquals(session, dataStorage.submitSession(session));
    assertEquals(1, dataStorage.retrieveAllSessions().size());
    assertTrue(dataStorage.retrieveAllSessions().contains(session));
  }

  @Test
  void stopSession() throws ChargingSessionException {
    ChargeSession session0 = new ChargeSession("ABC-12345", LocalDateTime.now());
    ChargeSession session1 = new ChargeSession("ABC-12345", LocalDateTime.now());
    UUID id0 = dataStorage.submitSession(session0).getId();
    dataStorage.submitSession(session1);
    assertEquals(StatusEnum.IN_PROGRESS, ((ChargeSession) dataStorage.retrieveAllSessions().toArray()[0]).getStatus());
    assertEquals(StatusEnum.IN_PROGRESS, ((ChargeSession) dataStorage.retrieveAllSessions().toArray()[1]).getStatus());

    dataStorage.stopSession(id0);

    assertEquals(StatusEnum.IN_PROGRESS, ((ChargeSession) dataStorage.retrieveAllSessions().toArray()[0]).getStatus());
    assertEquals(StatusEnum.FINISHED, ((ChargeSession) dataStorage.retrieveAllSessions().toArray()[1]).getStatus());
    assertEquals(id0, ((ChargeSession) dataStorage.retrieveAllSessions().toArray()[1]).getId());
  }

  @Test
  void retrieveAllSessions() {
    dataStorage.submitSession(new ChargeSession("ABC-12345", LocalDateTime.now()));
    dataStorage.submitSession(new ChargeSession("ABC-12345", LocalDateTime.now()));
    dataStorage.submitSession(new ChargeSession("ABC-12345", LocalDateTime.now()));
    assertEquals(3, dataStorage.retrieveAllSessions().size());
  }

  @Test
  void retrieveSummarySubmittedSessions() throws ChargingSessionException {
    dataStorage.submitSession(new ChargeSession("ABC-12345", LocalDateTime.now().minusMinutes(1)));
    ChargeSession session = new ChargeSession("ABC-12345", LocalDateTime.now().minusMinutes(2));
    dataStorage.submitSession(session);
    dataStorage.submitSession(new ChargeSession("ABC-12345", LocalDateTime.now().minusSeconds(40)));
    dataStorage.submitSession(new ChargeSession("ABC-12345", LocalDateTime.now().minusSeconds(30)));
    dataStorage.submitSession(new ChargeSession("ABC-12345", LocalDateTime.now().minusSeconds(20)));
    dataStorage.submitSession(new ChargeSession("ABC-12345", LocalDateTime.now().minusSeconds(10)));
    dataStorage.stopSession(session.getId());
    assertEquals(StatusEnum.FINISHED, session.getStatus());

    assertEquals(4, dataStorage.retrieveSummarySubmittedSessions().getStartedCount());
    assertEquals(1, dataStorage.retrieveSummarySubmittedSessions().getStoppedCount());
    assertEquals(5, dataStorage.retrieveSummarySubmittedSessions().getTotalCount());
  }

  @Test
  void sessionValidationNullSession() {
    Assertions.assertThrows(ChargingSessionException.class, () -> {
      SessionDataStorageImpl.sessionValidation(UUID.randomUUID(), null);
    });
  }

  @Test
  void sessionValidationFinishedSession() throws ChargingSessionException {
    ChargeSession session = new ChargeSession("ABC-12345", LocalDateTime.now());
    assertEquals(session, dataStorage.submitSession(session));
    dataStorage.stopSession(session.getId());

    Assertions.assertThrows(ChargingSessionException.class, () -> {
      SessionDataStorageImpl.sessionValidation(session.getId(), session);
    });
  }

  @Test
  void isChangedLessThanMinuteAgo() {
    assertFalse(SessionDataStorageImpl.isChangedLessThanMinuteAgo(LocalDateTime.now(), LocalDateTime.now().minusMinutes(1)));
    assertFalse(SessionDataStorageImpl.isChangedLessThanMinuteAgo(LocalDateTime.now(), LocalDateTime.now().minusMinutes(2)));
    assertTrue(SessionDataStorageImpl.isChangedLessThanMinuteAgo(LocalDateTime.now(), LocalDateTime.now().minusSeconds(20)));
  }
}