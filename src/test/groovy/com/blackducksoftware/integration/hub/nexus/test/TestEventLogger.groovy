/*
 * hub-nexus
 *
 * 	Copyright (C) 2019 Black Duck Software, Inc.
 * 	http://www.blackducksoftware.com/
 *
 * 	Licensed to the Apache Software Foundation (ASF) under one
 * 	or more contributor license agreements. See the NOTICE file
 * 	distributed with this work for additional information
 * 	regarding copyright ownership. The ASF licenses this file
 * 	to you under the Apache License, Version 2.0 (the
 * 	"License"); you may not use this file except in compliance
 * 	with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing,
 * 	software distributed under the License is distributed on an
 * 	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * 	KIND, either express or implied. See the License for the
 * 	specific language governing permissions and limitations
 * 	under the License.
 */
package com.blackducksoftware.integration.hub.nexus.test

import org.slf4j.Logger
import org.slf4j.Marker

import com.blackducksoftware.integration.test.TestLogger

public class TestEventLogger extends TestLogger implements Logger {

    public String getLastInfoLog() {
        return getOutputList().get(getOutputList().size() - 1)
    }

    @Override
    public String getName() {
        return "TestEventLogger"
    }

    @Override
    public boolean isTraceEnabled() {
        return true
    }

    @Override
    public void trace(final String format, final Object arg) {
        super.trace(String.format(format, arg))
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        super.trace(String.format(format, arg1, arg2))
    }

    @Override
    public void trace(final String format, final Object... arguments) {
        super.trace(String.format(format, arguments))
    }

    @Override
    public boolean isDebugEnabled() {
        return true
    }

    @Override
    public void debug(final String format, final Object arg) {
        super.debug(String.format(format, arg))
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        super.debug(String.format(format, arg1, arg2))
    }

    @Override
    public void debug(final String format, final Object... arguments) {
        super.debug(String.format(format, arguments))
    }

    @Override
    public boolean isInfoEnabled() {
        return true
    }

    @Override
    public void info(final String format, final Object arg) {
        super.info(String.format(format, arg))
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        super.info(String.format(format, arg1, arg2))
    }

    @Override
    public void info(final String format, final Object... arguments) {
        super.info(String.format(format, arguments))
    }

    @Override
    public boolean isWarnEnabled() {
        return true
    }

    @Override
    public void warn(final String format, final Object arg) {
        super.warn(String.format(format, arg))
    }

    @Override
    public void warn(final String format, final Object... arguments) {
        super.warn(String.format(format, arguments))
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        super.warn(String.format(format, arg1, arg2))
    }

    @Override
    public boolean isErrorEnabled() {
        return true
    }

    @Override
    public void error(final String format, final Object arg) {
        super.error(String.format(format, arg))
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        super.error(String.format(format, arg1, arg2))
    }

    @Override
    public void error(final String format, final Object... arguments) {
        super.error(String.format(format, arguments))
    }

    /*
     * Unimplemented
     */

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return false
    }

    @Override
    public void trace(final Marker marker, final String msg) {
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... argArray) {
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return false
    }

    @Override
    public void debug(final Marker marker, final String msg) {
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... arguments) {
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
    }

    @Override
    public void info(final String msg, final Throwable t) {
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return false
    }

    @Override
    public void info(final Marker marker, final String msg) {
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
    }

    @Override
    public void warn(final String msg, final Throwable t) {
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return false
    }

    @Override
    public void warn(final Marker marker, final String msg) {
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return false
    }

    @Override
    public void error(final Marker marker, final String msg) {
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void error(final Marker marker, final String format, final Object... arguments) {
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
    }
}
