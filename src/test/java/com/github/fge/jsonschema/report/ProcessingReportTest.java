/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jsonschema.report;

import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static com.github.fge.jsonschema.matchers.ProcessingMessageAssert.assertMessage;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public final class ProcessingReportTest
{
    /*
     * All levels except fatal
     */
    private static final EnumSet<LogLevel> LEVELS
        = EnumSet.complementOf(EnumSet.of(LogLevel.FATAL, LogLevel.NONE));

    @DataProvider
    public Iterator<Object[]> getLogLevels()
    {
        final List<Object[]> list = Lists.newArrayList();

        for (final LogLevel level: LEVELS)
            list.add(new Object[] { level });

        // We don't want the values in the same order repeatedly, so...
        Collections.shuffle(list);

        return list.iterator();
    }

    @Test(dataProvider = "getLogLevels")
    public void logThresholdIsRespected(final LogLevel logLevel)
        throws ProcessingException
    {
        final ProcessingReport report = spy(new LogThreshold(logLevel));
        final ProcessingMessage message = new ProcessingMessage();
        // OK, that's ugly, but it works...
        final int count = LogLevel.FATAL.ordinal() - logLevel.ordinal();

        report.debug(message);
        report.info(message);
        report.warn(message);
        report.error(message);

        verify(report, times(count)).log(any(LogLevel.class), same(message));
    }

    @Test
    public void logLevelIsCorrectlySetInMessages()
        throws ProcessingException
    {
        final ProcessingReport report = new LogThreshold(LogLevel.FATAL);
        final ProcessingMessage message = new ProcessingMessage();

        report.debug(message);
        assertMessage(message).hasLevel(LogLevel.DEBUG);
        report.info(message);
        assertMessage(message).hasLevel(LogLevel.INFO);
        report.warn(message);
        assertMessage(message).hasLevel(LogLevel.WARNING);
        report.error(message);
        assertMessage(message).hasLevel(LogLevel.ERROR);
    }

    @Test(dataProvider = "getLogLevels")
    public void exceptionThresholdIsRespected(final LogLevel logLevel)
    {
        final ProcessingReport report = new LogThreshold(LogLevel.DEBUG);
        report.setExceptionThreshold(logLevel);
        final ProcessingMessage message = new ProcessingMessage();
        final int expected = LogLevel.FATAL.ordinal() - logLevel.ordinal();
        int actual = 0;

        try {
            report.debug(message);
        } catch (ProcessingException ignored) {
            actual++;
        }
        try {
            report.info(message);
        } catch (ProcessingException ignored) {
            actual++;
        }
        try {
            report.warn(message);
        } catch (ProcessingException ignored) {
            actual++;
        }
        try {
            report.error(message);
        } catch (ProcessingException ignored) {
            actual++;
        }

        assertEquals(actual, expected);
    }

    private static class LogThreshold
        extends ProcessingReport
    {
        private LogThreshold(final LogLevel logThreshold)
        {
            setLogLevel(logThreshold);
        }

        @Override
        public void log(final LogLevel level, final ProcessingMessage message)
        {
        }

        @Override
        public final List<ProcessingMessage> getMessages()
        {
            return null;
        }
    }
}