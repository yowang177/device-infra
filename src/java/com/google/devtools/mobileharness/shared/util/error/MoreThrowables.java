/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.mobileharness.shared.util.error;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

import java.util.Map.Entry;
import java.util.stream.Stream;

/** More utilities for {@link Throwable}. */
public class MoreThrowables {

  /** Returns a formatted string of stack traces of all threads of the current process. */
  public static String formatStackTraces() {
    Thread currentThread = Thread.currentThread();
    return Thread.getAllStackTraces().entrySet().stream()
        .filter(threadEntry -> !threadEntry.getKey().equals(currentThread))
        .sorted(comparing(threadEntry -> threadEntry.getKey().getName()))
        .map(MoreThrowables::formatThread)
        .collect(joining("\n\n"));
  }

  private static String formatThread(Entry<Thread, StackTraceElement[]> threadEntry) {
    StringBuilder result = new StringBuilder();
    result.append(threadEntry.getKey());
    for (StackTraceElement stackTraceElement : threadEntry.getValue()) {
      result.append("\n\t");
      result.append(stackTraceElement);
    }
    return result.toString();
  }

  /**
   * Equivalent to {@linkplain #shortDebugString(Throwable, int) shortDebugString(e, 0)} (print
   * until the last cause).
   *
   * <p>Returns a string containing {@link Throwable#getMessage()} of the given exception and all of
   * its causes (if any). The delimiter string is "{@code , cause: }". For example, "{@code
   * message_of_e, cause: message_of_cause_of_e, cause: message_of_cause_of_cause_of_e}".
   *
   * <p>Note that the purpose of this method is "printing the content of an exception but making the
   * result string NOT be like an exception", for the cases in which you don't want the "\tat
   * xxx.xxx.Xxx(Xxx.java:xxx)" in log confuse users that here is the root cause. The result string
   * of this method may have bad readability. In most cases, you should use {@code
   * Throwables.getStackTraceAsString(e)} instead.
   *
   * @see #shortDebugString(Throwable, int)
   */
  public static String shortDebugString(Throwable e) {
    return shortDebugString(e, /* maxLength= */ 0);
  }

  /**
   * Returns a string containing {@link Throwable#getMessage()} of the given exception and all of
   * its causes (if any). The delimiter string is "{@code , cause: }". For example, "{@code
   * message_of_e, cause: message_of_cause_of_e, cause: message_of_cause_of_cause_of_e}".
   *
   * <p>Note that the purpose of this method is "printing the content of an exception but making the
   * result string NOT be like an exception", for the cases in which you don't want the "\tat
   * xxx.xxx.Xxx(Xxx.java:xxx)" in log confuse users that here is the root cause. The result string
   * of this method may have bad readability. In most cases, you should use {@code
   * Throwables.getStackTraceAsString(e)} instead.
   *
   * @param maxLength the max length of the printed message chain. For example, 2 means printing the
   *     message of the exception and the message of the cause of the exception (if any). 0 or
   *     negative for "no max length" (print until the last cause)
   */
  public static String shortDebugString(Throwable e, int maxLength) {
    StringBuilder result = new StringBuilder();
    boolean checkLength = maxLength > 0;
    while (true) {
      result.append(e.getMessage());
      if (checkLength) {
        maxLength--;
        if (maxLength == 0) {
          break;
        }
      }
      e = e.getCause();
      if (e == null) {
        break;
      }
      result.append(", cause: ");
    }
    return result.toString();
  }

  /**
   * Returns a short debug string representing the stack trace of the current thread. For example,
   * if {@code foo()} calls {@code goo()} and {@code goo()} calls this method, then the method will
   * return a string like:
   *
   * <pre>
   * {@code "[com.google.Goo.goo(Goo.java:10)  <-- com.google.Foo.foo(Foo.java:20)]"}
   * </pre>
   *
   * <p>Note that the purpose of this method is "printing a stack trace but making the result string
   * NOT be like an exception", for the cases in which you don't want the "\tat
   * xxx.xxx.Xxx(Xxx.java:xxx)" in log confuse users that here is the root cause. The result string
   * of this method may have bad readability. In most cases, you should use {@code
   * logger.atInfo().withStackTrace().log()} instead.
   *
   * @param maxLength the max length of the stack trace. For example, 2 means printing the first two
   *     layers of the stack trace. 0 or negative for "no max length" (print the full stack trace)
   */
  public static String shortDebugCurrentStackTrace(long maxLength) {
    // 2L to remove the stack trace of this method.
    return shortDebugCurrentStackTrace(maxLength, /* skip= */ 1L);
  }

  /**
   * Returns a short debug string representing the stack trace of the current thread. For example,
   * if {@code foo()} calls {@code goo()} and {@code goo()} calls this method, then the method will
   * return a string like:
   *
   * <pre>
   * {@code "[com.google.Goo.goo(Goo.java:10)  <-- com.google.Foo.foo(Foo.java:20)]"}
   * </pre>
   *
   * <p>Note that the purpose of this method is "printing a stack trace but making the result string
   * NOT be like an exception", for the cases in which you don't want the "\tat
   * xxx.xxx.Xxx(Xxx.java:xxx)" in log confuse users that here is the root cause. The result string
   * of this method may have bad readability. In most cases, you should use {@code
   * logger.atInfo().withStackTrace().log()} instead.
   *
   * @param maxLength the max length of the stack trace. For example, 2 means printing the first two
   *     layers of the stack trace. 0 or negative for "no max length" (print the full stack trace)
   * @param skip the number of layers to remove from the top. For example, if foo() calls goo(),
   *     goo() calls hoo(), and hoo() calls this method, if {@code skip} is {@code 1L}, the result
   *     will be like "{@code [com.google.Goo.goo(Goo.java:10) <--
   *     com.google.Foo.foo(Foo.java:20)]}"
   */
  public static String shortDebugCurrentStackTrace(long maxLength, long skip) {
    // 2L to remove the stack traces of Thread#getStackTrace() and this method.
    return shortDebugStackTrace(
        stream(Thread.currentThread().getStackTrace()).skip(2L + skip), maxLength);
  }

  /**
   * Returns a short debug string representing the stack trace of the given thread. For example,
   *
   * <pre>
   * {@code "[com.google.Goo.goo(Goo.java:10)  <-- com.google.Foo.foo(Foo.java:20)]"}
   * </pre>
   *
   * <p>Note that the purpose of this method is "printing a stack trace but making the result string
   * NOT be like an exception", for the cases in which you don't want the "\tat
   * xxx.xxx.Xxx(Xxx.java:xxx)" in log confuse users that here is the root cause. The result string
   * of this method may have bad readability. In most cases, you should use {@code
   * logger.atInfo().withStackTrace().log()} instead.
   *
   * @param maxLength the max length of the stack trace. For example, 2 means printing the first two
   *     layers of the stack trace. 0 or negative for "no max length" (print the full stack trace)
   */
  public static String shortDebugStackTrace(Thread thread, long maxLength) {
    return shortDebugStackTrace(stream(thread.getStackTrace()), maxLength);
  }

  /**
   * Returns a short debug string representing the stack trace of the given exception. For example,
   *
   * <pre>
   * {@code "[com.google.Goo.goo(Goo.java:10)  <-- com.google.Foo.foo(Foo.java:20)]"}
   * </pre>
   *
   * <p>Note that the purpose of this method is "printing a stack trace but making the result string
   * NOT be like an exception", for the cases in which you don't want the "\tat
   * xxx.xxx.Xxx(Xxx.java:xxx)" in log confuse users that here is the root cause. The result string
   * of this method may have bad readability. In most cases, you should use {@code
   * logger.atInfo().withStackTrace().log()} instead.
   *
   * @param maxLength the max length of the stack trace. For example, 2 means printing the first two
   *     layers of the stack trace. 0 or negative for "no max length" (print the full stack trace)
   */
  public static String shortDebugStackTrace(Throwable throwable, long maxLength) {
    return shortDebugStackTrace(stream(throwable.getStackTrace()), maxLength);
  }

  private static String shortDebugStackTrace(Stream<StackTraceElement> stackTrace, long maxLength) {
    return stackTrace
        .limit(maxLength <= 0 ? Long.MAX_VALUE : maxLength)
        .map(StackTraceElement::toString)
        .collect(joining("  <--  ", "[", "]"));
  }

  private MoreThrowables() {}
}
