package com.assignment.carstore.components;

import com.assignment.carstore.domain.ChargeSession;
import com.assignment.carstore.domain.CounterSummary;
import java.util.UUID;

/**
 * Charging sessions data storage interface.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
public interface SessionDataStorage extends DataStorage<UUID, ChargeSession, CounterSummary> {
}
