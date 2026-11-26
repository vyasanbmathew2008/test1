package com.rocky.portscanner.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PortParserTest {
    @Test
    public void parsesRangesAndListsWithoutDuplicates() {
        assertEquals(Arrays.asList(20, 21, 22, 80, 443),
                PortParser.parse("20-22,80,443,21"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOutOfRangePorts() {
        PortParser.parse("0,80");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsReversedRanges() {
        PortParser.parse("100-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMalformedEntries() {
        PortParser.parse("80,,443");
    }
}
