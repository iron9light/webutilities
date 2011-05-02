/*
 * Copyright 2010-2011 Rajendra Patil
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.googlecode.webutilities.modules;

import com.googlecode.webutilities.common.ServletResponseWrapper;

import javax.servlet.ServletRequestWrapper;

public interface IRule {

    public void process(ServletRequestWrapper request, ServletResponseWrapper response);

}

/**
 * Example of the file that will contain rules
 *
 * when url =~ regex and mime =~ regex or browser =~ regex
 * Header Request unset Something
 * Header Response set MyHeader SomeValue
 * Header Response set Expires "access plus 1 year"
 * Compression threshold 10000
 * Cache reloadTime value resetTime value
 * Charset UTF-8 force true
 * YUICompress noMunge value ... <optionName> <optionValue>
 * JSCSSMerge tunOffEtag true autoCorrectUrlsInCSS true
 * Access Allow from all
 * Access Deny from some-ip/netmask/subnet etc.
 * Closure <option> <value>
 *
 */