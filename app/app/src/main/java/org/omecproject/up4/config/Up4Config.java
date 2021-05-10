/*
 SPDX-License-Identifier: LicenseRef-ONF-Member-1.0
 SPDX-FileCopyrightText: 2020-present Open Networking Foundation <info@opennetworking.org>
 */
package org.omecproject.up4.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents the config expected from a UPF network configuration JSON block.
 */
public class Up4Config extends Config<ApplicationId> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    // JSON keys to look for in the network config
    public static final String KEY = "up4";  // base key that signals the presence of this config
    public static final String MAX_UES = "maxUes";
    public static final String DEVICE_ID = "deviceId";
    public static final String UE_POOLS = "uePools";
    public static final String S1U_PREFIX = "s1uPrefix";  // TODO: remove this field after all configs updated
    public static final String S1U_ADDR = "s1uAddr";
    public static final String DBUF_DRAIN_ADDR = "dbufDrainAddr";
    // FIXME: remove defaultQfi and pscEncapEnabled once we expose QFI in logical pipeline
    //  QFI should be set by the SMF using PFCP
    public static final String DEFAULT_QFI = "defaultQfi";
    public static final String PSC_ENCAP_ENABLED = "pscEncapEnabled";


    @Override
    public boolean isValid() {
        return hasOnlyFields(DEVICE_ID, UE_POOLS, S1U_ADDR, S1U_PREFIX,
                DBUF_DRAIN_ADDR, MAX_UES, PSC_ENCAP_ENABLED, DEFAULT_QFI) &&
                // Mandatory fields.
                hasFields(DEVICE_ID, UE_POOLS) &&
                (hasField(S1U_ADDR) || hasField(S1U_PREFIX)) &&
                !uePools().isEmpty() &&
                isDbufConfigValid();
    }

    private boolean isDbufConfigValid() {
        if (dbufDrainAddr() != null) {
            try {
                // Force the drain address string to be parsed
                dbufDrainAddr();
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the UP4 ONOS device ID.
     *
     * @return UP4 device ID
     */
    public DeviceId up4DeviceId() {
        return DeviceId.deviceId(object.path(DEVICE_ID).asText());
    }

    /**
     * Get the S1U IPv4 address assigned to the device. Or null if not properly configured.
     *
     * @return The S1U IPv4 address assigned to the device
     */
    public Ip4Address s1uAddress() {
        if (hasField(S1U_ADDR)) {
            String addr = get(S1U_ADDR, null);
            return addr != null ? Ip4Address.valueOf(addr) : null;
        } else if (hasField(S1U_PREFIX)) {
            // TODO: remove this whole block after all network configs have been updated
            log.warn("UP4 config field {} has been replaced by {}, please update your config!",
                    S1U_PREFIX, S1U_ADDR);
            String prefix = get(S1U_PREFIX, null);
            if (prefix == null) {
                return null;
            }
            try {
                // Try converting to a prefix just to check that the format is correct
                Ip4Prefix.valueOf(prefix);
            } catch (Exception e) {
                return null;
            }
            // We can't do Ip4Prefix.address() because it masks off the host bits
            String[] pieces = prefix.split("/");
            return Ip4Address.valueOf(pieces[0]);
        } else {
            return null;
        }
    }

    /**
     * Set the S1U IPv4 address of the device.
     *
     * @param addr The S1U IPv4 address to assign
     * @return an updated instance of this config
     */
    public Up4Config setS1uAddr(String addr) {
        return (Up4Config) setOrClear(S1U_ADDR, addr);
    }

    /**
     * Gets the list of UE IPv4 address pools assigned to the device. Or null if not configured.
     *
     * @return UE IPv4 address pools assigned to the device
     */
    public List<Ip4Prefix> uePools() {
        if (!object.has(UE_POOLS)) {
            return null;
        }
        List<Ip4Prefix> uePools = new ArrayList<>();
        ArrayNode uePoolsNode = (ArrayNode) object.path(UE_POOLS);
        for (JsonNode uePoolNode : uePoolsNode) {
            String uePoolString = uePoolNode.asText("");
            if (uePoolString.equals("")) {
                return null;
            }
            uePools.add(Ip4Prefix.valueOf(uePoolString));
        }
        return ImmutableList.copyOf(uePools);
    }

    /**
     * Returns the address of the UPF interface that the dbuf device will drain packets towards, or null
     * if not configured.
     *
     * @return the address of the upf interface that receives packets drained from dbuf
     */
    public Ip4Address dbufDrainAddr() {
        String addr = get(DBUF_DRAIN_ADDR, null);
        return addr != null ? Ip4Address.valueOf(addr) : null;
    }

    /**
     * Returns the maximum number of UEs the UPF can support, or -1 if not configured.
     * @return the maximum number of UEs the UPF can support
     */
    public long maxUes() {
        return get(MAX_UES, -1);
    }

    /**
     * Returns whether the UPF should use GTP-U extension PDU Session Container when doing encap of
     * downlink packets.
     *
     * @return whether PSC encap is enabled
     */
    public boolean pscEncapEnabled() {
        return get(PSC_ENCAP_ENABLED, false);
    }

    /**
     * Returns the default QoS Flow Identifier to use when PSC encap is enabled.
     *
     * @return whether PSC encap is enabled
     */
    public int defaultQfi() {
        return get(DEFAULT_QFI, 0);
    }
}

