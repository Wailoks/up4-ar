/*
 * SPDX-License-Identifier: LicenseRef-ONF-Member-1.0
 * SPDX-FileCopyrightText: {year}-present Open Networking Foundation <info@opennetworking.org>
 */

package org.omecproject.up4;

import com.google.common.annotations.Beta;
import org.onosproject.event.AbstractEvent;

/**
 * An event generated by Up4Service.
 */
@Beta
public class Up4Event extends AbstractEvent<Up4Event.Type, Up4EventSubject> {

    /**
     * Types of event.
     */
    public enum  Type {
        /**
         * Signals that the data plane device has detected a downlink packet for a UE in buffering
         * state.
         */
        DOWNLINK_DATA_NOTIFICATION
    }

    /**
     * Creates a new Up4Event.
     *
     * @param type type of event
     * @param subject subject
     */
    public Up4Event(Type type, Up4EventSubject subject) {
        super(type, subject);
    }
}
