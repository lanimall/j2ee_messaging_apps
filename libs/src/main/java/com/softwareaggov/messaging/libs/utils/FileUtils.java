/*
 * Copyright © 2016 - 2018 Software AG, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.softwareaggov.messaging.libs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJBException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by fabien.sanglier on 7/4/18.
 */
public class FileUtils {
    private static Logger log = LoggerFactory.getLogger(FileUtils.class);

    static public URL getFileURL(String filePath) throws MalformedURLException {
        URL url = null;
        if (filePath.toLowerCase().startsWith("classpath:")) {
            filePath = filePath.substring("classpath:".length());
            url = FileUtils.class.getClassLoader().getResource(filePath);
        } else if (filePath.toLowerCase().startsWith("file:")) {
            filePath = filePath.substring("file:".length());
            url = new File(filePath).toURI().toURL();
        } else { //default to file
            url = new File(filePath).toURI().toURL();
        }
        return url;
    }

    static public String getFileContent(String filePath) throws IOException {
        String fileRawStr = null;
        if (null != filePath && !"".equals(filePath)) {
            InputStream inputStream = null;
            try {
                inputStream = getFileURL(filePath).openStream();
                if (null != inputStream) {
                    StringBuilder textBuilder = new StringBuilder();
                    Reader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader
                                (inputStream, Charset.forName("UTF-8")));
                        int c = 0;
                        while ((c = reader.read()) != -1) {
                            textBuilder.append((char) c);
                        }
                    } finally {
                        if (null != reader) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                throw new EJBException("Could not close the reader", e);
                            }
                        }
                    }
                    fileRawStr = textBuilder.toString();
                }
            } catch (IOException e) {
                log.error("Could not load the content of file identified by path: " + filePath, e);
                throw e;
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("Could not close the input stream", e);
                        throw e;
                    }
                }
            }
        } else {
            log.debug("No path specified for content file...ignoring");
        }
        return fileRawStr;
    }
}
