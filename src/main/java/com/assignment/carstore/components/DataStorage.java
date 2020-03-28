package com.assignment.carstore.components;

import com.assignment.carstore.exceptions.ChargingSessionException;

import java.util.Collection;

/**
 * Data storage interface.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
public interface DataStorage<U, T, O> {

  /**
   * Submit a new charging session for the station.
   */
  T submitSession(T session);

  /**
   * Stop charging session.
   */
  T stopSession(U id) throws ChargingSessionException;

  /**
   * Retrieve all charging sessions.
   */
  Collection<T> retrieveAllSessions();

  /**
   * Retrieve a summary of submitted charging sessions including
   * totalCount, startedCount, stoppedCount.
   */
  O retrieveSummarySubmittedSessions();
}
