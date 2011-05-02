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

package com.googlecode.webutilities.filters;

import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.parsing.Config;
import com.googlecode.webutilities.common.*;
import com.googlecode.webutilities.filters.common.AbstractFilter;
import com.googlecode.webutilities.util.Utils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.googlecode.webutilities.common.Constants.*;

/**
 * Filter that performs minification using Google Closure Compiler
 *
 * @author rpatil
 * @version 1.0
 */
public class ClosureCompilerFilter extends AbstractFilter{

    private static final Logger LOGGER = Logger.getLogger(ClosureCompilerFilter.class.getName());

    CompilerOptions compilerOptions;

    JSSourceFile nullExtern = JSSourceFile.fromCode("/dev/null", "");

    private static final String PROCESSED_ATTR = YUIMinFilter.class.getName() + ".MINIFIED";

    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        compilerOptions = buildCompilerOptionsFromConfig(config);
        LOGGER.config("Filter initialized with: " +
                "{" + compilerOptions.toString() +
                "}");
    }
    //init
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

        //build compiler options
        //create compiler object
        //compile, get result
        //write result to output stream
        HttpServletRequest rq = (HttpServletRequest) req;

        HttpServletResponse rs = (HttpServletResponse) resp;

        String url = rq.getRequestURI(), lowerUrl = url.toLowerCase();

        LOGGER.fine("Filtering URI: " + url);

        boolean alreadyProcessed = req.getAttribute(PROCESSED_ATTR) != null;

        if (!alreadyProcessed && isURLAccepted (url) && isUserAgentAccepted(rq.getHeader(Constants.HTTP_USER_AGENT_HEADER)) && (lowerUrl.endsWith(EXT_JS) || lowerUrl.endsWith(EXT_JSON) || lowerUrl.endsWith(EXT_CSS))) {

            req.setAttribute(PROCESSED_ATTR, Boolean.TRUE);

            com.googlecode.webutilities.common.ServletResponseWrapper wrapper = new com.googlecode.webutilities.common.ServletResponseWrapper(rs);
            //Let the response be generated

            chain.doFilter(req, wrapper);

            Writer out = resp.getWriter();
            String mime = wrapper.getContentType();
            if(!isMIMEAccepted(mime)){
                out.write(wrapper.getContents());
                out.flush();
                LOGGER.finest("Not minifying. Mime (" + mime + ") not allowed.");
                return;
            }

            ByteArrayInputStream is = new ByteArrayInputStream(wrapper.getBytes());

            //work on generated response
            if (lowerUrl.endsWith(EXT_JS) || lowerUrl.endsWith(EXT_JSON) || (wrapper.getContentType() != null && (wrapper.getContentType().equals(MIME_JS) || wrapper.getContentType().equals(MIME_JSON)))) {
                Compiler closureCompiler = new Compiler(new LoggerErrorManager(LOGGER));
                LOGGER.finest("Compressing JS/JSON type");
                CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
                level.setOptionsForCompilationLevel(compilerOptions);
                Result result = closureCompiler.compile(nullExtern,JSSourceFile.fromInputStream(null,is), compilerOptions);
                if(result.success){
                    out.append(closureCompiler.toSource());
                }
            } else {
                LOGGER.finest("Not Compressing anything.");
                out.write(wrapper.getContents());
            }

            out.flush();
        } else {
            LOGGER.finest("Not minifying. URL/UserAgent not allowed.");
            chain.doFilter(req, resp);
        }
    }

    private static CompilerOptions buildCompilerOptionsFromConfig(FilterConfig config){

        CompilerOptions compilerOptions = new CompilerOptions();
        compilerOptions.setCodingConvention(new DefaultCodingConvention());
        //List<String> processedArgs = Lists.newArrayList();
        Enumeration<String> initParams = config.getInitParameterNames();
        while(initParams.hasMoreElements()){
            String name = initParams.nextElement().trim();
            String value = config.getInitParameter(name);
            if("acceptConstKeyword".equals(name)){
                compilerOptions.setAcceptConstKeyword(Utils.readBoolean(value, false));
            }else if("charset".equals(name)){
                compilerOptions.setOutputCharset(Utils.readString(value, "UTF-8"));
            }else if("compilationLevel".equals(name)){
                CompilationLevel compilationLevel = CompilationLevel.valueOf(value);
                compilationLevel.setOptionsForCompilationLevel(compilerOptions);
            }else if("formatting".equals(name)){
                if("PRETTY_PRINT".equals(value)){
                    compilerOptions.prettyPrint = true;
                }else if("PRINT_INPUT_DELIMITER".equals(value)){
                    compilerOptions.printInputDelimiter = true;
                }
            }else if("loggingLevel".equals(name)){
                Compiler.setLoggingLevel(Level.parse(value));
            }
        }
        return compilerOptions;
    }

}
