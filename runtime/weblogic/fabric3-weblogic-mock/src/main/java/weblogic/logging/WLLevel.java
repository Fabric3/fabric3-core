/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package weblogic.logging;

import com.bea.logging.LogLevel;

/**
 *
 */
public class WLLevel extends LogLevel {
    private static final long serialVersionUID = 3016995389747701017L;
    public static WLLevel ALERT = new WLLevel("", 0);

    public static WLLevel CRITICAL = new WLLevel("", 0);

    public static WLLevel DEBUG = new WLLevel("", 0);

    public static WLLevel EMERGENCY = new WLLevel("", 0);

    public static WLLevel ERROR = new WLLevel("", 0);

    public static WLLevel INFO = new WLLevel("", 0);

    public static WLLevel NOTICE = new WLLevel("", 0);

    public static WLLevel OFF
            = new WLLevel("", 0);
    public static WLLevel TRACE = new WLLevel("", 0);

    public static WLLevel WARNING = new WLLevel("", 0);

    public WLLevel(String name, int value) {
        super(name, value);
    }
}
