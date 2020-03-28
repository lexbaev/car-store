package com.assignment.carstore.components.impl;

import com.assignment.carstore.components.SessionDataStorage;
import com.assignment.carstore.domain.ChargeSession;
import com.assignment.carstore.domain.CounterSummary;
import com.assignment.carstore.domain.StatusEnum;
import com.assignment.carstore.exceptions.ChargingSessionException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of charging sessions data storage based on LinkedHashMap.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
@Component
public class SessionDataStorageImpl implements SessionDataStorage {

  /**
   * Charging sessions storage map.
   */
  private Map<UUID, ChargeSession> chargeSessionMap = new LinkedHashMap<>();

  /**
   * Locks the map for updating.
   */
  private Lock lock = new ReentrantLock();

  @Override
  public ChargeSession submitSession(ChargeSession session) {
    lock.lock();
    try {
      chargeSessionMap.put(session.getId(), session);
    } finally {
      lock.unlock();
    }
    return session;
  }

  @Override
  public ChargeSession stopSession(UUID id) throws ChargingSessionException {
    lock.lock();
    ChargeSession session;
    try {
      session = chargeSessionMap.get(id);
      sessionValidation(id, session);
      chargeSessionMap.remove(id);
      session.setStoppedAt(LocalDateTime.now());
      session.setStatus(StatusEnum.FINISHED);
      chargeSessionMap.put(session.getId(), session);
    } finally {
      lock.unlock();
    }
    return session;
  }

  @Override
  public Collection<ChargeSession> retrieveAllSessions() {
    return chargeSessionMap.values();
  }

  @Override
  public CounterSummary retrieveSummarySubmittedSessions() {
    LocalDateTime now = LocalDateTime.now();
    long startedCount = 0, stoppedCount = 0;

    List<UUID> keyList = new ArrayList<>(chargeSessionMap.keySet());
    for (int i = keyList.size() - 1; i > -1; i--) {
      ChargeSession session = chargeSessionMap.get(keyList.get(i));
      if (session.getStatus() == StatusEnum.FINISHED && isChangedLessThanMinuteAgo(now, session.getStoppedAt())) {
        stoppedCount++;
      } else if (session.getStatus() == StatusEnum.IN_PROGRESS && isChangedLessThanMinuteAgo(now, session.getStartedAt())) {
        startedCount++;
      } else {
        break;
      }
    }
    return new CounterSummary(startedCount + stoppedCount, startedCount, stoppedCount);
  }

  /**
   * Validate the charging session is not null and the session has not been finished.
   *
   * @param id
   * @param session
   * @throws ChargingSessionException
   */
  protected static void sessionValidation(UUID id, ChargeSession session) throws ChargingSessionException {
    if (session == null) {
      throw new ChargingSessionException(String.format("Charging session with id: %s is not found", id), HttpStatus.BAD_REQUEST.value());
    } else if (session.getStatus() == StatusEnum.FINISHED) {
      throw new ChargingSessionException(String.format("Charging session with id: %s has already been finished", id), HttpStatus.BAD_REQUEST.value());
    }
  }

  /**
   * Returns true if loggedTime value is not earlier than one minute ago.
   *
   * @param now
   * @param loggedTime
   * @return
   */
  protected static boolean isChangedLessThanMinuteAgo(LocalDateTime now, LocalDateTime loggedTime) {
    return loggedTime.isAfter(now.minusMinutes(1L));
  }
}
