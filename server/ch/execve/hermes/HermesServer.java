// Copyright 2025 Paweł Zuzelski <pawelz@execve.ch>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ch.execve.hermes;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HermesServer {

    private static void configureLogging(String logFile) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder.start();

        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);

        if (logFile == null) {
            ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
            appender.setContext(loggerContext);
            appender.setEncoder(encoder);
            appender.start();
            rootLogger.addAppender(appender);
        } else {
            FileAppender<ILoggingEvent> appender = new FileAppender<>();
            appender.setContext(loggerContext);
            appender.setFile(logFile);
            appender.setEncoder(encoder);
            appender.start();
            rootLogger.addAppender(appender);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("boop"); // A polite way to greet the user.
        CommandLineArgs flags = CommandLineArgs.getFlags(args);
        configureLogging(flags.getLogFile());
        final Logger logger = LoggerFactory.getLogger(HermesServer.class);

        logger.info("Using config directory: {}", flags.getConfigDir());
        logger.info("Using database path: {}", flags.getDatabasePath());

        Injector injector = Guice.createInjector(new HermesModule(flags));
        SocketListener socketListener = injector.getInstance(SocketListener.class);
        socketListener.start();
    }
}
