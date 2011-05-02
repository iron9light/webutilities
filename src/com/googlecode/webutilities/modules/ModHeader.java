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

public class ModHeader implements IModule{

    public static final String IDENTIFIER = "Header";

    private static enum Directive{
        Request,
        Response
    }
    private static enum Condition{
        onsuccess,
        always
    }

    private static enum Action{
        set,
        append,
        add,
        unset,
        echo
    }

    public static class RequestRule implements IRule{

        Action action;

        String headerName;

        String headerValue;

        public RequestRule(Action action, String headerName, String headerValue) {
            this.action = action;
            this.headerName = headerName;
            this.headerValue = headerValue;
        }

        @Override
        public void process(ServletRequestWrapper request, ServletResponseWrapper response) {
            /*switch (action){
                case add:
                    request.addHeader(headerName, headerValue);
                    break;
                case set:
                    request.setHeader(headerName, headerValue);
                    break;
                case append:
                    request.appendHeader(headerName, headerValue);
                    break;
                case unset:
                    request.unsetHeader(headerName, headerValue);
                    break;

            }*/
        }
    }

    public static class ResponseRule extends RequestRule{

        Condition condition;

        public ResponseRule(Condition condition, Action action, String headerName, String headerValue) {
            super(action, headerName, headerValue);
            this.condition = condition;
        }

        public void process(ServletRequestWrapper request, ServletResponseWrapper response) {

            if(condition.equals(Condition.always) ||
                    (condition.equals(Condition.onsuccess) &&
                     response.getStatus() >= 200 && response.getStatus() < 300)){

                //!TODO avoid replaceAll
//                headerValue = headerValue.replaceAll("%("+FormatSpecifier.t.name()+")", "$1="+request.getTime());
//                headerValue = headerValue.replaceAll("%("+FormatSpecifier.D.name()+")", "$1="+new Date().getTime() - request.getTime());

                /*switch (action){
                    case add:
                        response.addHeader(headerName, headerValue);
                        break;
                    case set:
                        response.setHeader(headerName, headerValue);
                        break;
                    case append:
                        response.appendHeader(headerName, headerValue);
                        break;
                    case unset:
                        response.unsetHeader(headerName, headerValue);
                        break;
                    case echo:
                        //!TODO
                        break;
                }*/

            }

        }
    }

    @Override
        public IRule parseRule(String ruleString) {
            int index = 0;

            String[] splits = ruleString.split("\\s+");

            assert splits.length >= 4;

            if(!splits[index].equals(IDENTIFIER)) return null;

            IRule rule = null;
            Directive directive;
            Condition condition = Condition.always;
            Action action;

            String headerName;
            String headerValue;

            directive = Directive.valueOf(splits[index++]);

            assert directive != null;

            if(directive.equals(Directive.Response)){
                if(splits[index].equals(Condition.onsuccess.name()) || splits[index].equals(Condition.always.name())){
                    condition = Condition.valueOf(splits[index++]);
                }
            }

            action = Action.valueOf(splits[index++]);

            assert action != null;

            headerName = splits[index++];

            headerValue = splits[index];

            return directive.equals(Directive.Response) ?
                    new ResponseRule(condition, action, headerName, headerValue)
                    : new RequestRule(action, headerName, headerValue);

        }

}
